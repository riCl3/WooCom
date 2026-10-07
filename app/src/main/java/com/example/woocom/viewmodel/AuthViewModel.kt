package com.example.woocom.viewmodel

import androidx.lifecycle.ViewModel
import com.example.woocom.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Login failed")
                }
            }
    }

    fun signup(email: String, password: String, name: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onResult(false, task.exception?.localizedMessage ?: "Sign up failed")
                    return@addOnCompleteListener
                }

                val userId = task.result?.user?.uid
                if (userId == null) {
                    onResult(false, "User ID is null")
                    return@addOnCompleteListener
                }

                val userModel = UserModel(name = name, email = email, userId = userId)
                firestore.collection("user").document(userId)
                    .set(userModel)
                    .addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            onResult(true, null)
                        } else {
                            onResult(false, dbTask.exception?.localizedMessage ?: "Could not save profile")
                        }
                    }
            }
    }
}