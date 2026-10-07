package com.example.woocom.data

import com.example.woocom.model.OrderModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.roundToInt

/** Line items and total handed to [PaymentGateway.beginCheckout]. */
data class CheckoutRequest(
    val items: Map<String, Long>,
    val amount: Double,
    val itemCount: Int,
)

/**
 * Everything the settlement callbacks need to identify the purchase.
 *
 * [razorpayOrderId] is only set when the server created the Razorpay order (Supabase);
 * on the Firestore path Razorpay generates its own id and only [orderId] matters.
 */
data class CheckoutSession(
    val orderId: String,
    val amountPaise: Int,
    val razorpayOrderId: String? = null,
)

/**
 * Order lifecycle: create a `pending` order, then settle it from the Razorpay callbacks.
 *
 * Having both backends behind one interface is what lets [com.example.woocom.MainActivity]
 * stay backend-agnostic: the security-relevant differences (who computes the amount, who
 * may flip a status) live inside the implementations.
 */
interface PaymentGateway {
    /** Writes the pending order and returns what Razorpay checkout needs. */
    suspend fun beginCheckout(request: CheckoutRequest): CheckoutSession

    /** Marks the order paid after Razorpay reported success. */
    suspend fun settlePayment(
        session: CheckoutSession,
        paymentId: String,
        signature: String?,
    )

    /** Records why checkout failed so the Orders screen can explain it. */
    suspend fun failPayment(
        session: CheckoutSession,
        reason: String,
    )
}

/**
 * Legacy path: the client computes the total and writes its own `pending` order document,
 * then flips it to `paid`/`failed` on the callback. Kept as the fallback backend so the
 * app still runs without Supabase credentials.
 */
class FirestorePaymentGateway(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth,
) : PaymentGateway {
    override suspend fun beginCheckout(request: CheckoutRequest): CheckoutSession {
        val userId =
            auth.currentUser?.uid
                ?: throw IllegalStateException("Please sign in to continue.")
        val doc = db.collection(ORDERS).document()
        doc.set(
            OrderModel(
                orderId = doc.id,
                userId = userId,
                amount = request.amount,
                itemCount = request.itemCount,
                items = request.items,
                status = OrderModel.STATUS_PENDING,
                createdAt = System.currentTimeMillis(),
            ),
        ).await()
        return CheckoutSession(orderId = doc.id, amountPaise = toPaise(request.amount))
    }

    override suspend fun settlePayment(
        session: CheckoutSession,
        paymentId: String,
        signature: String?,
    ) {
        db.collection(ORDERS).document(session.orderId)
            .update(
                mapOf(
                    "status" to OrderModel.STATUS_PAID,
                    "paymentId" to paymentId,
                ),
            ).await()
    }

    override suspend fun failPayment(
        session: CheckoutSession,
        reason: String,
    ) {
        db.collection(ORDERS).document(session.orderId)
            .update(
                mapOf(
                    "status" to OrderModel.STATUS_FAILED,
                    "failureReason" to reason.take(MAX_REASON_LENGTH),
                ),
            ).await()
    }

    private fun toPaise(amount: Double): Int = (amount * 100).roundToInt()

    private companion object {
        const val ORDERS = "orders"
        const val MAX_REASON_LENGTH = 200
    }
}

/**
 * Server-trusted path: `create-razorpay-order` recomputes the amount from the cart rows
 * and writes the pending order, and `verify-payment` re-derives Razorpay's HMAC signature
 * with the key secret before the `mark_order_paid` RPC flips the status. The client never
 * names a price and cannot mark its own order paid.
 */
class SupabasePaymentGateway(
    private val supabase: SupabaseClient = SupabaseBackend.client,
) : PaymentGateway {
    override suspend fun beginCheckout(request: CheckoutRequest): CheckoutSession {
        val payload = postJson(CREATE_ORDER_FUNCTION, "{}").decodeOrThrow<CreateOrderResponse>()
        return CheckoutSession(
            orderId = payload.supabaseOrderId,
            amountPaise = payload.amount,
            razorpayOrderId = payload.razorpayOrderId,
        )
    }

    override suspend fun settlePayment(
        session: CheckoutSession,
        paymentId: String,
        signature: String?,
    ) {
        val razorpayOrderId =
            session.razorpayOrderId
                ?: throw IllegalStateException("Payment is missing its Razorpay order id")
        val paymentSignature =
            signature
                ?: throw IllegalStateException("Payment is missing its signature — cannot verify")
        postJson(
            VERIFY_PAYMENT_FUNCTION,
            Json.encodeToString(
                SettleBody(
                    paymentId = paymentId,
                    razorpayOrderId = razorpayOrderId,
                    signature = paymentSignature,
                    supabaseOrderId = session.orderId,
                ),
            ),
        )
    }

    override suspend fun failPayment(
        session: CheckoutSession,
        reason: String,
    ) {
        supabase.postgrest.rpc(
            MARK_ORDER_FAILED,
            buildJsonObject {
                put(PARAM_ORDER_ID, session.orderId)
                put(PARAM_REASON, reason.take(MAX_REASON_LENGTH))
            },
        )
    }

    /** POSTs [body] to an Edge Function and returns the raw response body. */
    private suspend fun postJson(
        function: String,
        body: String,
    ): String {
        val response =
            supabase.functions.invoke(function) {
                method = HttpMethod.Post
                setBody(TextContent(body, ContentType.Application.Json))
            }
        val text = response.bodyAsText()
        if (response.status.value !in 200..299) {
            throw IllegalStateException(errorMessageOf(text))
        }
        return text
    }

    private inline fun <reified T> String.decodeOrThrow(): T =
        try {
            Json.decodeFromString<T>(this)
        } catch (error: Throwable) {
            throw IllegalStateException(errorMessageOf(this), error)
        }

    private fun errorMessageOf(body: String): String =
        runCatching { Json.decodeFromString<ErrorBody>(body).error }
            .getOrDefault(body.ifBlank { "The server could not complete the request" })

    @Serializable
    private data class CreateOrderResponse(
        val supabaseOrderId: String,
        val razorpayOrderId: String,
        val amount: Int,
        val currency: String = "INR",
    )

    @Serializable
    private data class SettleBody(
        val paymentId: String,
        val razorpayOrderId: String,
        val signature: String,
        @SerialName("supabaseOrderId")
        val supabaseOrderId: String,
    )

    @Serializable
    private data class ErrorBody(val error: String = "")

    private companion object {
        const val CREATE_ORDER_FUNCTION = "create-razorpay-order"
        const val VERIFY_PAYMENT_FUNCTION = "verify-payment"
        const val MARK_ORDER_FAILED = "mark_order_failed"
        const val PARAM_ORDER_ID = "p_order_id"
        const val PARAM_REASON = "p_reason"
        const val MAX_REASON_LENGTH = 200
    }
}
