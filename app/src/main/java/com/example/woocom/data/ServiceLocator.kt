package com.example.woocom.data

/**
 * Composition-root wiring for the data layer.
 *
 * Keeping construction in one place means screens never `new`-up a repository, and a
 * future backend swap (Firestore → Supabase) is a single-line change here plus the
 * implementation class.
 */
object ServiceLocator {
    val productRepository: ProductRepository by lazy { FirestoreProductRepository() }
    val userRepository: UserRepository by lazy { FirestoreUserRepository() }
}
