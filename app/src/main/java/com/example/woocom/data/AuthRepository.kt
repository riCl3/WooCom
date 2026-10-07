package com.example.woocom.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException

/** Thrown by [AuthRepository] with a message that is safe to show in the UI. */
class AuthException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Authentication behind a backend-neutral interface.
 *
 * Screens only ever see [AuthException] messages, so swapping Firebase Auth for Supabase
 * Auth is invisible to the UI layer (the choice is made in `ServiceLocator`).
 */
interface AuthRepository {
    fun currentUserId(): String?

    fun isSignedIn(): Boolean = currentUserId() != null

    suspend fun signIn(
        email: String,
        password: String,
    )

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
    )

    suspend fun signOut()
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = Firebase.auth,
) : AuthRepository {
    override fun currentUserId(): String? = auth.currentUser?.uid

    override suspend fun signIn(
        email: String,
        password: String,
    ) {
        withAuthErrors { auth.signInWithEmailAndPassword(email, password).await() }
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
    ) {
        withAuthErrors { auth.createUserWithEmailAndPassword(email, password).await() }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    private suspend fun <T> withAuthErrors(block: suspend () -> T): T =
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            throw authException(error)
        }

    private fun authException(error: Throwable): AuthException =
        AuthException(
            message =
                when (error) {
                    is FirebaseAuthInvalidUserException -> "No account found with that email"
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password"
                    is FirebaseAuthUserCollisionException -> "An account with that email already exists"
                    is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters"
                    is IOException -> "Can't reach the server — check your connection"
                    else -> error.localizedMessage ?: "Something went wrong"
                },
            cause = error,
        )
}

class SupabaseAuthRepository(
    private val supabase: SupabaseClient = SupabaseBackend.client,
) : AuthRepository {
    override fun currentUserId(): String? = supabase.auth.currentSessionOrNull()?.user?.id

    override suspend fun signIn(
        email: String,
        password: String,
    ) {
        val address = email
        val secret = password
        withAuthErrors {
            supabase.auth.signInWith(Email) {
                this.email = address
                this.password = secret
            }
        }
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
    ) {
        val address = email
        val secret = password
        val displayName = name
        withAuthErrors {
            // The metadata is read by the `handle_new_user` trigger in Postgres, so the
            // profile row carries the display name before the client ever writes it.
            supabase.auth.signUpWith(Email) {
                this.email = address
                this.password = secret
                data = buildJsonObject { put("name", displayName) }
            }
        }
    }

    override suspend fun signOut() {
        withAuthErrors { supabase.auth.signOut() }
    }

    private suspend fun <T> withAuthErrors(block: suspend () -> T): T =
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            throw authException(error)
        }

    private fun authException(error: Throwable): AuthException {
        val description = (error as? AuthRestException)?.errorDescription ?: error.message.orEmpty()
        return AuthException(
            message =
                when {
                    error is AuthWeakPasswordException -> "Password must be at least 6 characters"
                    description.contains("Invalid login credentials", ignoreCase = true) ->
                        "Incorrect email or password"
                    description.contains("already registered", ignoreCase = true) ->
                        "An account with that email already exists"
                    description.contains("User not found", ignoreCase = true) ->
                        "No account found with that email"
                    description.contains("Email not confirmed", ignoreCase = true) ->
                        "Confirm your email before signing in"
                    error is IOException || error.cause is IOException ->
                        "Can't reach the server — check your connection"
                    else -> description.ifBlank { "Something went wrong" }
                },
            cause = error,
        )
    }
}
