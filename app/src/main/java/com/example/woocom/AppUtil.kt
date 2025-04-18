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
                //Check if current quantity is initialized or not, if not, initialize with 0
                val currentQuantity = currentCart[productId]?:0
                val updatedQuantity = currentQuantity + 1
                //Updating the quantity
                val updatedCart = mapOf("cartItems.$productId" to updatedQuantity)
                //Push this updated quantity to Firebase
                userDoc.update(updatedCart)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            showToast(context, "Item added to cart")
                        }else{
                            showToast(context, "Failed to add item to cart")
                        }
                    }
            }
        }
    }
}