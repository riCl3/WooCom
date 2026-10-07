package com.example.woocom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woocom.data.ServiceLocator
import com.example.woocom.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/** Screen-level authentication state. */
sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Submitting : AuthUiState
    data object Success : AuthUiState
    data class Failure(val message: String) : AuthUiState
}

/**
 * Authentication entry point.
 *
 * Exposes a single [StateFlow] instead of per-call callbacks so screens cannot forget to
 * clear their loading flag, and maps Firebase exceptions to messages a user can act on
 * rather than surfacing raw SDK text.
 */
class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        _state.value = AuthUiState.Submitting
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { _state.value = AuthUiState.Success }
            .addOnFailureListener { _state.value = AuthUiState.Failure(friendlyMessage(it)) }
    }

    fun signup(name: String, email: String, password: String) {
        _state.value = AuthUiState.Submitting
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid
                if (userId == null) {
                    _state.value = AuthUiState.Failure("Could not create your account")
                    return@addOnSuccessListener
                }
                val userModel = UserModel(name = name, email = email, userId = userId)
                // Profile write goes through the repository so the document id rule
                // (one doc per uid) lives in exactly one place.
                viewModelScope.launch {
                    runCatching { ServiceLocator.userRepository.saveProfile(userModel) }
                        .onSuccess { _state.value = AuthUiState.Success }
                        .onFailure { _state.value = AuthUiState.Failure(friendlyMessage(it)) }
                }
            }
            .addOnFailureListener { _state.value = AuthUiState.Failure(friendlyMessage(it)) }
    }

    /** Called once the screen has handled a success so the state can be reused. */
    fun consumeSuccess() {
        if (_state.value is AuthUiState.Success) _state.value = AuthUiState.Idle
    }

    fun clearError() {
        if (_state.value is AuthUiState.Failure) _state.value = AuthUiState.Idle
    }

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is FirebaseAuthInvalidUserException -> "No account found with that email"
        is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password"
        is FirebaseAuthUserCollisionException -> "An account with that email already exists"
        is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters"
        is IOException -> "Can't reach the server — check your connection"
        else -> error.localizedMessage ?: "Something went wrong"
    }
}
