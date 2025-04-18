package com.example.woocom.model

data class UserModel(
    val name : String = "",
    val email : String = "",
    val userId : String = "",
    val cartItems : Map<String,Long> = emptyMap(),
)
