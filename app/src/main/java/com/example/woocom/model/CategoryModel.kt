package com.example.woocom.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** [Name] keeps its historical capital to avoid touching every call site. */
@Serializable
data class CategoryModel(
    val id: String = "",
    @SerialName("name")
    val Name: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
)
