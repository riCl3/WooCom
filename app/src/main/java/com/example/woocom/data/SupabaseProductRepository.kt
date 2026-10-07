package com.example.woocom.data

import com.example.woocom.model.CategoryModel
import com.example.woocom.model.ProductModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable

/**
 * Postgres-backed catalogue reads.
 *
 * Compared with the Firestore implementation this moves filtering server-side: search is
 * an `ILIKE` query instead of a full-collection download, and the batched id lookup is a
 * single `in (...)` statement rather than N queries.
 */
class SupabaseProductRepository(
    private val supabase: SupabaseClient = SupabaseBackend.client,
) : ProductRepository {
    private fun products() = supabase.postgrest.from(PRODUCTS)

    override suspend fun products(limit: Int): List<ProductModel> {
        val take = limit
        return products()
            .select {
                order("id", Order.ASCENDING)
                limit(take.toLong())
            }.decodeList<ProductModel>()
    }

    override suspend fun categories(): List<CategoryModel> =
        supabase.postgrest.from(CATEGORIES)
            .select {}
            .decodeList<CategoryModel>()

    override suspend fun banners(): List<String> =
        supabase.postgrest.from(BANNERS)
            .select {}
            .decodeList<BannerRow>()
            .flatMap { it.urls }

    override suspend fun productById(productId: String): ProductModel? =
        products()
            .select { filter { eq("id", productId) } }
            .decodeList<ProductModel>()
            .firstOrNull()

    override suspend fun productsByIds(productIds: Collection<String>): List<ProductModel> {
        if (productIds.isEmpty()) return emptyList()
        return productIds.distinct()
            .chunked(ID_IN_CHUNK)
            .flatMap { chunk ->
                products()
                    .select { filter { isIn("id", chunk) } }
                    .decodeList<ProductModel>()
            }
    }

    override suspend fun productsInCategory(categoryId: String): List<ProductModel> =
        products()
            .select { filter { eq("category", categoryId) } }
            .decodeList<ProductModel>()

    override suspend fun searchProducts(query: String): List<ProductModel> {
        val needle = query.trim()
        if (needle.isEmpty()) return emptyList()
        val pattern = "%${needle.escapeLike()}%"
        return products()
            .select {
                filter {
                    or {
                        ilike("title", pattern)
                        ilike("category", pattern)
                    }
                }
            }.decodeList<ProductModel>()
    }

    /** Postgres `LIKE` treats `%` and `_` as wildcards, so a literal search must escape them. */
    private fun String.escapeLike(): String =
        replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")

    @Serializable
    private data class BannerRow(val urls: List<String> = emptyList())

    private companion object {
        const val PRODUCTS = "products"
        const val CATEGORIES = "categories"
        const val BANNERS = "banners"

        /** Keeps the generated `in (...)` clause comfortably inside URL length limits. */
        const val ID_IN_CHUNK = 100
    }
}
