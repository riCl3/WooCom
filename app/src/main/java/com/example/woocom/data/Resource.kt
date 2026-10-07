package com.example.woocom.data

/**
 * Minimal UI-facing async state.
 *
 * Using a sealed type instead of nullable pairs / separate loading booleans means the
 * screen *must* handle loading, success and failure — an empty list caused by a network
 * error is no longer indistinguishable from "there is genuinely nothing here".
 */
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>

    data class Success<T>(val data: T) : Resource<T>

    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>

    val dataOrNull: T?
        get() = (this as? Success)?.data

    val errorMessageOrNull: String?
        get() = (this as? Error)?.message

    val isLoading: Boolean
        get() = this is Loading
}

/** Runs [block] and converts any thrown exception into [Resource.Error]. */
suspend fun <T> resourceOf(block: suspend () -> T): Resource<T> =
    try {
        Resource.Success(block())
    } catch (t: Throwable) {
        Resource.Error(
            message = t.localizedMessage ?: "Something went wrong",
            cause = t,
        )
    }
