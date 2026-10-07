package com.example.woocom

import android.content.Context
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import java.util.Locale

/**
 * Shared helper utilities: cart / favourites mutations against Firestore.
 *
 * All operations read the currently signed-in user.
 */
object AppUtil {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Safely parses a currency string (e.g. "1,299.00") into a Double.
     */
    fun parsePrice(price: String): Double {
        return try {
            price.replace(",", "").trim().toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    fun formatPrice(amount: Double): String =
        String.format(Locale.getDefault(), "₹%,.2f", amount)

    /** Returns the current user id or null when not signed in. */
    private fun currentUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    /** Document reference for the signed-in user, or null when signed out. */
    fun userDocument(): com.google.firebase.firestore.DocumentReference? {
        val uid = currentUserId() ?: return null
        return Firebase.firestore.collection("user").document(uid)
    }

    fun addToCart(productId: String, context: Context) {
        val userDoc = userDocument() ?: return
        userDoc.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentCart = task.result.get("cartItems") as? Map<String, Long> ?: emptyMap()
                val updatedQuantity = (currentCart[productId] ?: 0L) + 1L
                userDoc.update("cartItems.$productId", updatedQuantity)
                    .addOnCompleteListener { update ->
                        val message =
                            if (update.isSuccessful) "Item added to cart"
                            else "Failed to add item to cart"
                        showToast(context, message)
                    }
            }
        }
    }

    fun removeFromCart(
        productId: String,
        context: Context,
        onDone: ((Boolean) -> Unit)? = null
    ) {
        val userDoc = userDocument()
        if (userDoc == null) {
            showToast(context, "Please sign in first")
            onDone?.invoke(false)
            return
        }
        userDoc.update("cartItems.$productId", FieldValue.delete())
            .addOnSuccessListener { onDone?.invoke(true) }
            .addOnFailureListener {
                if (onDone == null) showToast(context, "Failed to remove item from cart")
                onDone?.invoke(false)
            }
    }

    fun addToFavorites(productId: String, context: Context) {
        val userDoc = userDocument() ?: return
        userDoc.update("favorites.$productId", true)
            .addOnCompleteListener { task ->
                val message =
                    if (task.isSuccessful) "Added to favourites"
                    else "Failed to add to favourites"
                showToast(context, message)
            }
    }

    fun removeFromFavorites(productId: String, context: Context) {
        val userDoc = userDocument() ?: return
        userDoc.update("favorites.$productId", FieldValue.delete())
            .addOnCompleteListener { task ->
                val message =
                    if (task.isSuccessful) "Removed from favourites"
                    else "Failed to remove from favourites"
                showToast(context, message)
            }
    }

    fun isFavorite(productId: String, callback: (Boolean) -> Unit) {
        val userDoc = userDocument() ?: run {
            callback(false)
            return
        }
        userDoc.get().addOnSuccessListener { snapshot ->
            val favorites = snapshot.get("favorites") as? Map<String, Boolean> ?: emptyMap()
            callback(favorites[productId] == true)
        }.addOnFailureListener {
            callback(false)
        }
    }
}