package com.example.woocom.model

data class ProductModel(
    val id: String = " ",
    val title: String = " ",
    val description: String = " ",
    val categoty: String = " ",
    val price: String = " ",
    val actualPrice: String = " ",
    val images : List<String> = emptyList()
)
