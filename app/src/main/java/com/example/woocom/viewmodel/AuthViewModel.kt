package com.example.woocom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woocom.data.AuthRepository
import com.example.woocom.data.Resource
import com.example.woocom.data.ServiceLocator
import com.example.woocom.data.UserRepository
import com.example.woocom.data.resourceOf
import com.example.woocom.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
 * clear their loading flag. Both [AuthRepository] implementations map their SDK's errors
 * to user-facing messages, so this class holds no SDK-specific logic at all.
 */
class AuthViewModel(
    private val auth: AuthRepository = ServiceLocator.authRepository,
    private val users: UserRepository = ServiceLocator.userRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(
        email: String,
        password: String,
    ) {
        _state.value = AuthUiState.Submitting
        viewModelScope.launch {
            _state.value = toUiState(resourceOf { auth.signIn(email, password) })
        }
    }

    fun signup(
        name: String,
        email: String,
        password: String,
    ) {
        _state.value = AuthUiState.Submitting
        viewModelScope.launch {
            val result =
                resourceOf {
                    auth.signUp(name, email, password)
                    // Profile write goes through the repository so the "one row/document
                    // per uid" rule lives in exactly one place. Supabase provisions the
                    // row from the signup trigger, so this is an idempotent merge there.
                    val uid = auth.currentUserId()
                    if (uid != null) {
                        users.saveProfile(UserModel(name = name, email = email, userId = uid))
                    }
                }
            _state.value = toUiState(result)
        }
    }

    /** Called once the screen has handled a success so the state can be reused. */
    fun consumeSuccess() {
        if (_state.value is AuthUiState.Success) _state.value = AuthUiState.Idle
    }

    fun clearError() {
        if (_state.value is AuthUiState.Failure) _state.value = AuthUiState.Idle
    }

    private fun toUiState(result: Resource<Unit>): AuthUiState =
        when (result) {
            is Resource.Error -> AuthUiState.Failure(result.message)
            else -> AuthUiState.Success
        }
}
