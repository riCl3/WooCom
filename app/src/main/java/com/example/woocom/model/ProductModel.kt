package com.example.woocom.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A catalogue item.
 *
 * Field names follow the Firestore document; the `@SerialName` annotations map the same
 * object onto the snake_case Postgres columns in `supabase/migrations`.
 */
@Serializable
data class ProductModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: String = "",
    @SerialName("actual_price")
    val actualPrice: String = "",
    val images: List<String> = emptyList(),
    @Serializable(with = AnyMapAsJsonObject::class)
    @SerialName("other_details")
    val otherDetails: Map<String, Any> = emptyMap(),
)
