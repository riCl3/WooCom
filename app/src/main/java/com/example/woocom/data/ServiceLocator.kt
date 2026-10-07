package com.example.woocom.data

/**
 * Composition-root wiring for the data layer.
 *
 * Keeping construction in one place means screens never `new`-up a repository, and the
 * Firestore → Supabase swap is decided once here: if the project was configured with
 * `SUPABASE_URL`/`SUPABASE_ANON_KEY` (see `gradle.properties`) the Postgres
 * implementations are used, otherwise the original Firestore ones keep working so the
 * app runs for anyone who clones it without backend credentials.
 */
object ServiceLocator {
    val isSupabaseBackend: Boolean = SupabaseBackend.isConfigured

    val authRepository: AuthRepository by lazy {
        if (isSupabaseBackend) SupabaseAuthRepository() else FirebaseAuthRepository()
    }

    val productRepository: ProductRepository by lazy {
        if (isSupabaseBackend) SupabaseProductRepository() else FirestoreProductRepository()
    }

    val userRepository: UserRepository by lazy {
        if (isSupabaseBackend) SupabaseUserRepository() else FirestoreUserRepository()
    }

    val paymentGateway: PaymentGateway by lazy {
        if (isSupabaseBackend) SupabasePaymentGateway() else FirestorePaymentGateway()
    }
}
