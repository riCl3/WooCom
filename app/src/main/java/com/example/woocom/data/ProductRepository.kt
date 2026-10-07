package com.example.woocom.data

import com.example.woocom.model.CategoryModel
import com.example.woocom.model.ProductModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Read-side access to the catalogue.
 *
 * Every screen goes through this interface so the data source (Firestore today,
 * Postgres/Supabase later) can be swapped without touching UI code, and so a single
 * screen can share one in-flight fetch instead of issuing the same query per composable.
 */
interface ProductRepository {
    /** Newest [limit] products from the catalogue. */
    suspend fun products(limit: Int): List<ProductModel>

    suspend fun categories(): List<CategoryModel>

    suspend fun banners(): List<String>

    /** Looks a product up by its `id` field (not necessarily the document id). */
    suspend fun productById(productId: String): ProductModel?

    /** Batched lookup used by cart rows to avoid an N+1 query pattern. */
    suspend fun productsByIds(productIds: Collection<String>): List<ProductModel>

    suspend fun productsInCategory(categoryId: String): List<ProductModel>

    /**
     * Client-side filter over the full catalogue. Firestore has no prefix index on
     * arbitrary fields, so this is deliberately documented as a stop-gap.
     */
    suspend fun searchProducts(query: String): List<ProductModel>
}

class FirestoreProductRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) : ProductRepository {

    private fun products() = db.collection(DATA).document(STOCK).collection(PRODUCTS)

    override suspend fun products(limit: Int): List<ProductModel> =
        products().limit(limit.toLong())
            .get().await().toObjects(ProductModel::class.java)

    override suspend fun categories(): List<CategoryModel> =
        db.collection(DATA).document(STOCK).collection(CATEGORIES)
            .get().await().toObjects(CategoryModel::class.java)

    override suspend fun banners(): List<String> =
        db.collection(DATA).document(BANNER)
            .get().await().get("urls") as? List<String> ?: emptyList()

    override suspend fun productById(productId: String): ProductModel? =
        products().whereEqualTo("id", productId).limit(1)
            .get().await().toObjects(ProductModel::class.java).firstOrNull()

    override suspend fun productsByIds(productIds: Collection<String>): List<ProductModel> {
        if (productIds.isEmpty()) return emptyList()
        return productIds.chunked(FIRESTORE_WHERE_IN_LIMIT)
            .flatMap { chunk ->
                products().whereIn("id", chunk).get().await()
                    .toObjects(ProductModel::class.java)
            }
    }

    override suspend fun productsInCategory(categoryId: String): List<ProductModel> =
        products().whereEqualTo("category", categoryId)
            .get().await().toObjects(ProductModel::class.java)

    /**
     * Client-side filter over the full catalogue — Firestore cannot run a prefix match on
     * `title` without a dedicated index, so the (small) demo dataset is filtered locally.
     * The Supabase migration replaces this with an `ILIKE`/trigram query.
     */
    override suspend fun searchProducts(query: String): List<ProductModel> {
        val needle = query.trim()
        if (needle.isEmpty()) return emptyList()
        return products().get().await()
            .toObjects(ProductModel::class.java)
            .filter {
                it.title.contains(needle, ignoreCase = true) ||
                    it.category.contains(needle, ignoreCase = true)
            }
    }

    private companion object {
        const val DATA = "data"
        const val STOCK = "stock"
        const val PRODUCTS = "products"
        // Spelled this way in the live backend; corrected in the Supabase schema.
        const val CATEGORIES = "categoties"
        const val BANNER = "banner"
        const val FIRESTORE_WHERE_IN_LIMIT = 30
    }
}
