package com.example.woocom.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A completed (or in-flight) purchase.
 *
 * The Firestore path writes this as an `orders` document after checkout; the Supabase
 * path reads the same shape back from the `orders` table, which is why [createdAt] is
 * annotated with [IsoDateTimeAsEpochMillis] — Postgres stores a `timestamptz`, Firestore
 * stored epoch milliseconds, and both now surface as the same [Long].
 */
@Serializable
data class OrderModel(
    @SerialName("id")
    val orderId: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("payment_id")
    val paymentId: String = "",
    /** Razorpay's own order id; empty until checkout opened (Firestore path never sets it). */
    @SerialName("razorpay_order_id")
    val razorpayOrderId: String = "",
    val amount: Double = 0.0,
    @SerialName("item_count")
    val itemCount: Int = 0,
    val items: Map<String, Long> = emptyMap(),
    val status: String = STATUS_PENDING,
    @Serializable(with = IsoDateTimeAsEpochMillis::class)
    @SerialName("created_at")
    val createdAt: Long = 0L,
    @SerialName("failure_reason")
    val failureReason: String = "",
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_PAID = "paid"
        const val STATUS_FAILED = "failed"
    }
}
