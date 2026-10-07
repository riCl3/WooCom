package com.example.woocom.data

import com.example.woocom.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest

/**
 * The Supabase backend, wired from `BuildConfig`.
 *
 * [isConfigured] is the single switch for the whole app: when the project has no
 * `SUPABASE_URL` (see `gradle.properties`) every repository falls back to Firestore, so
 * the app still runs for anyone who clones it without backend credentials.
 *
 * The anon key is public by design — it ships in every APK. Authorisation comes from the
 * Row Level Security policies in `supabase/migrations`, not from hiding the key.
 */
object SupabaseBackend {
    val isConfigured: Boolean = BuildConfig.SUPABASE_URL.isNotBlank()

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Postgrest)
            install(Auth)
            install(Functions)
        }
    }
}
