package com.example.woocom.data

import com.example.woocom.model.OrderModel
import com.example.woocom.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Write-side access to the signed-in user's document (profile, cart, favourites, orders).
 *
 * All mutations are atomic: cart quantities use [FieldValue.increment] and every field
 * path is built with [FieldPath.of] so a product id containing `.` or `/` cannot be
 * mis-parsed as a nested field.
 */
interface UserRepository {
    fun userDocument(): DocumentReference?

    /** Null when nobody is signed in. */
    fun currentUserId(): String?

    suspend fun currentUser(): UserModel?

    /** Creates/merges the signed-in user's profile document. */
    suspend fun saveProfile(user: UserModel)

    suspend fun addToCart(productId: String)

    suspend fun setCartQuantity(
        productId: String,
        quantity: Long,
    )

    suspend fun removeFromCart(productId: String)

    suspend fun clearCart()

    suspend fun setFavorite(
        productId: String,
        favorite: Boolean,
    )

    suspend fun isFavorite(productId: String): Boolean

    suspend fun placeOrder(order: OrderModel): String

    suspend fun markOrderPaid(
        orderId: String,
        paymentId: String,
    )

    suspend fun markOrderFailed(
        orderId: String,
        reason: String,
    )

    suspend fun ordersForUser(): List<OrderModel>
}

class FirestoreUserRepository(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth,
) : UserRepository {
    override fun userDocument(): DocumentReference? = auth.currentUser?.uid?.let { db.collection(USER_COLLECTION).document(it) }

    private fun requireUserDocument(): DocumentReference = userDocument() ?: throw IllegalStateException("You need to sign in first")

    override fun currentUserId(): String? = auth.currentUser?.uid

    override suspend fun currentUser(): UserModel? = userDocument()?.get()?.await()?.toObject(UserModel::class.java)

    override suspend fun addToCart(productId: String) {
        requireUserDocument()
            .update(FieldPath.of(CART_ITEMS, productId), FieldValue.increment(1))
            .await()
    }

    override suspend fun setCartQuantity(
        productId: String,
        quantity: Long,
    ) {
        val doc = requireUserDocument()
        if (quantity <= 0) {
            doc.update(FieldPath.of(CART_ITEMS, productId), FieldValue.delete()).await()
        } else {
            doc.update(FieldPath.of(CART_ITEMS, productId), quantity).await()
        }
    }

    override suspend fun removeFromCart(productId: String) {
        requireUserDocument()
            .update(FieldPath.of(CART_ITEMS, productId), FieldValue.delete())
            .await()
    }

    override suspend fun clearCart() {
        requireUserDocument().update(CART_ITEMS, emptyMap<String, Any>()).await()
    }

    override suspend fun setFavorite(
        productId: String,
        favorite: Boolean,
    ) {
        val doc = requireUserDocument()
        if (favorite) {
            doc.update(FieldPath.of(FAVORITES, productId), true).await()
        } else {
            doc.update(FieldPath.of(FAVORITES, productId), FieldValue.delete()).await()
        }
    }

    override suspend fun isFavorite(productId: String): Boolean {
        val snapshot = userDocument()?.get()?.await() ?: return false
        val favorites = snapshot.get(FAVORITES) as? Map<*, *> ?: return false
        return favorites[productId] == true
    }

    override suspend fun placeOrder(order: OrderModel): String {
        val doc = db.collection(ORDERS).document()
        doc.set(order.copy(orderId = doc.id)).await()
        return doc.id
    }

    override suspend fun markOrderPaid(
        orderId: String,
        paymentId: String,
    ) {
        db.collection(ORDERS).document(orderId)
            .update(
                mapOf(
                    "status" to OrderModel.STATUS_PAID,
                    "paymentId" to paymentId,
                ),
            ).await()
    }

    override suspend fun markOrderFailed(
        orderId: String,
        reason: String,
    ) {
        db.collection(ORDERS).document(orderId)
            .update(
                mapOf(
                    "status" to OrderModel.STATUS_FAILED,
                    "failureReason" to reason.take(MAX_REASON_LENGTH),
                ),
            ).await()
    }

    override suspend fun ordersForUser(): List<OrderModel> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return db.collection(ORDERS)
            .whereEqualTo("userId", uid)
            .get().await().toObjects(OrderModel::class.java)
    }

    override suspend fun saveProfile(user: UserModel) {
        requireUserDocument().set(user, SetOptions.merge()).await()
    }

    private companion object {
        const val USER_COLLECTION = "user"
        const val CART_ITEMS = "cartItems"
        const val FAVORITES = "favorites"
        const val ORDERS = "orders"
        const val MAX_REASON_LENGTH = 200
    }
}
