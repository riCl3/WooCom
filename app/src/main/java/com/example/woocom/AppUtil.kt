package com.example.woocom

import android.content.Context
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

object AppUtil {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun addToCart(productId: String, context: Context){
        val userDoc = Firebase.firestore.collection("user")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)

        userDoc.get().addOnCompleteListener {
            if(it.isSuccessful){
                val currentCart = it.result.get("cartItems") as? Map<String,Long> ?: emptyMap()
                val currentQuantity = currentCart[productId]?:0
                val updatedQuantity = currentQuantity + 1

                val updatedCart = mapOf("cartItems.$productId" to updatedQuantity)

                userDoc.update(updatedCart)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            showToast(context, "Item added to cart")
                        }else{
                            showToast(context, "Failed to add item to cart")
                        }
                    }
            }
        }
    }

    fun addToFavorites(productId: String, context: Context) {
        val userDoc = Firebase.firestore.collection("user")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)

        val update = mapOf("favorites.$productId" to true)

        userDoc.update(update).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast(context, "Added to favorites")
            } else {
                showToast(context, "Failed to add to favorites")
            }
        }
    }

    fun removeFromFavorites(productId: String, context: Context) {
        val userDoc = Firebase.firestore.collection("user")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)

        val update = mapOf("favorites.$productId" to null) // removes the field

        userDoc.update(update).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast(context, "Removed from favorites")
            } else {
                showToast(context, "Failed to remove from favorites")
            }
        }
    }

    fun isFavorite(productId: String, context: Context, callback: (Boolean) -> Unit) {
        val userDoc = Firebase.firestore.collection("user")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)

        userDoc.get().addOnSuccessListener {
            val favorites = it.get("favorites") as? Map<String, Boolean> ?: emptyMap()
            callback(favorites[productId] == true)
        }.addOnFailureListener {
            callback(false)
        }
    }
}
