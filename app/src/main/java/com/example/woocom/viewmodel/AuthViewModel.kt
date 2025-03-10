package com.example.woocom.viewmodel

import androidx.lifecycle.ViewModel
import com.example.woocom.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class AuthViewModel : ViewModel()  {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    fun signup(email: String, password: String, name: String, onResult: (Boolean,String?) -> Unit){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    var userId = it.result?.user?.uid
                    val usermodel = UserModel(name,email,userId!!)
                    firestore.collection("user").document(userId)
                        .set(usermodel)
                        .addOnCompleteListener { dbTask->
                            if(dbTask.isSuccessful){
                                onResult(true,null)
                            }else{
                                onResult(false,it.exception?.message.toString())
                            }
                        }
                }else{
                    onResult(false,it.exception?.message.toString())
                }
            }
    }
}