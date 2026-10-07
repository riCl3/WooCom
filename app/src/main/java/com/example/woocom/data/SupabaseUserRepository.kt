package com.example.woocom.data

import com.example.woocom.model.OrderModel
import com.example.woocom.model.UserModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Postgres-backed access to the signed-in user's data.
 *
 * Firestore kept profile, cart and favourites as maps inside one document; Postgres
 * normalises them into `profiles`, `cart_items` and `favorites`. [currentUser] re-composes
 * those rows into the same [UserModel] so no screen changes were needed, and every query
 * is additionally scoped by `user_id` — RLS enforces the same scope on the server even if
 * a query forgot it.
 */
class SupabaseUserRepository(
    private val supabase: SupabaseClient = SupabaseBackend.client,
) : UserRepository {
    override fun currentUserId(): String? = supabase.auth.currentSessionOrNull()?.user?.id

    private fun requireUserId(): String = currentUserId() ?: throw IllegalStateException("You need to sign in first")

    override suspend fun currentUser(): UserModel? {
        val uid = currentUserId() ?: return null
        val profile =
            supabase.postgrest.from(PROFILES)
                .select { filter { eq("id", uid) } }
                .decodeList<ProfileRow>()
                .firstOrNull()
        val cart =
            supabase.postgrest.from(CART_ITEMS)
                .select { filter { eq("user_id", uid) } }
                .decodeList<CartRow>()
        val favorites =
            supabase.postgrest.from(FAVORITES)
                .select { filter { eq("user_id", uid) } }
                .decodeList<FavoriteRow>()

        return UserModel(
            name = profile?.name ?: metadataName(),
            email = profile?.email ?: supabase.auth.currentSessionOrNull()?.user?.email.orEmpty(),
            userId = uid,
            cartItems = cart.associate { it.productId to it.quantity },
            favorites = favorites.associate { it.productId to true },
        )
    }

    /** Display name written into `raw_user_meta_data` at sign-up (see `handle_new_user`). */
    private fun metadataName(): String {
        val metadata = supabase.auth.currentSessionOrNull()?.user?.userMetadata ?: return ""
        return (metadata["name"] as? JsonPrimitive)?.content.orEmpty()
    }

    override suspend fun saveProfile(user: UserModel) {
        val uid = requireUserId()
        supabase.postgrest.from(PROFILES)
            .update(ProfileUpdate(name = user.name, email = user.email)) {
                filter { eq("id", uid) }
            }
    }

    override suspend fun addToCart(productId: String) {
        requireUserId()
        // Single atomic upsert-plus-increment in the database; the Firestore version
        // needed a FieldValue.increment for the same reason.
        supabase.postgrest.rpc(ADD_TO_CART, buildJsonObject { put(PARAM_PRODUCT_ID, productId) })
    }

    override suspend fun setCartQuantity(
        productId: String,
        quantity: Long,
    ) {
        val uid = requireUserId()
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        supabase.postgrest.from(CART_ITEMS)
            .upsert(
                CartRow(userId = uid, productId = productId, quantity = quantity),
            ) {
                onConflict = "$USER_ID,$PRODUCT_ID"
            }
    }

    override suspend fun removeFromCart(productId: String) {
        val uid = requireUserId()
        supabase.postgrest.from(CART_ITEMS)
            .delete {
                filter {
                    eq(USER_ID, uid)
                    eq(PRODUCT_ID, productId)
                }
            }
    }

    override suspend fun clearCart() {
        val uid = requireUserId()
        supabase.postgrest.from(CART_ITEMS)
            .delete { filter { eq(USER_ID, uid) } }
    }

    override suspend fun setFavorite(
        productId: String,
        favorite: Boolean,
    ) {
        val uid = requireUserId()
        if (favorite) {
            // ignoreDuplicates makes a repeated tap idempotent instead of a 409.
            supabase.postgrest.from(FAVORITES)
                .upsert(
                    FavoriteRow(userId = uid, productId = productId),
                ) {
                    onConflict = "$USER_ID,$PRODUCT_ID"
                    ignoreDuplicates = true
                }
        } else {
            supabase.postgrest.from(FAVORITES)
                .delete {
                    filter {
                        eq(USER_ID, uid)
                        eq(PRODUCT_ID, productId)
                    }
                }
        }
    }

    override suspend fun isFavorite(productId: String): Boolean {
        val uid = currentUserId() ?: return false
        val rows =
            supabase.postgrest.from(FAVORITES)
                .select {
                    filter {
                        eq(USER_ID, uid)
                        eq(PRODUCT_ID, productId)
                    }
                    limit(1)
                }.decodeList<FavoriteRow>()
        return rows.isNotEmpty()
    }

    override suspend fun ordersForUser(): List<OrderModel> {
        val uid = currentUserId() ?: return emptyList()
        return supabase.postgrest.from(ORDERS)
            .select {
                filter { eq(USER_ID, uid) }
                order(CREATED_AT, Order.DESCENDING)
            }.decodeList<OrderModel>()
    }

    /** A single table row per user, unlike the Firestore document which held nested maps. */
    @Serializable
    private data class ProfileRow(
        val id: String = "",
        val name: String = "",
        val email: String = "",
    )

    @Serializable
    private data class ProfileUpdate(
        val name: String,
        val email: String,
    )

    @Serializable
    private data class CartRow(
        @SerialName("user_id")
        val userId: String = "",
        @SerialName("product_id")
        val productId: String = "",
        val quantity: Long = 0,
    )

    @Serializable
    private data class FavoriteRow(
        @SerialName("user_id")
        val userId: String = "",
        @SerialName("product_id")
        val productId: String = "",
    )

    private companion object {
        const val PROFILES = "profiles"
        const val CART_ITEMS = "cart_items"
        const val FAVORITES = "favorites"
        const val ORDERS = "orders"

        const val USER_ID = "user_id"
        const val PRODUCT_ID = "product_id"
        const val CREATED_AT = "created_at"

        const val ADD_TO_CART = "add_to_cart"
        const val PARAM_PRODUCT_ID = "p_product_id"
    }
}
