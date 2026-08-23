package com.example.appagendamientocitas.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appagendamientocitas.data.local.SessionManager
import com.example.appagendamientocitas.data.local.entity.UserRole
import com.example.appagendamientocitas.domain.usecase.LoginUseCase
import com.example.appagendamientocitas.domain.usecase.RegisterClientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterClientUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = update { it.copy(password = value, errorMessage = null) }
    fun onNameChange(value: String) = update { it.copy(name = value, errorMessage = null) }
    fun toggleMode() = update {
        it.copy(isRegisterMode = !it.isRegisterMode, errorMessage = null)
    }

    fun submit(onSuccess: (UserRole) -> Unit) {
        val state = _uiState.value

        val validationError = validate(state)
        if (validationError != null) {
            update { it.copy(errorMessage = validationError) }
            return
        }

        update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = if (state.isRegisterMode) {
                registerUseCase(state.name, state.email, state.password)
            } else {
                loginUseCase(state.email, state.password)
            }

            result
                .onSuccess { user ->
                    sessionManager.saveSession(user)
                    update { it.copy(isLoading = false) }
                    onSuccess(user.role)
                }
                .onFailure { e ->
                    update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Error inesperado"
                        )
                    }
                }
        }
    }

    private fun validate(s: LoginUiState): String? {
        if (s.email.isBlank()) return "Ingresa tu usuario o email"
        if (s.password.isBlank()) return "Ingresa tu contraseña"
        if (s.isRegisterMode) {
            if (s.name.isBlank()) return "Ingresa tu nombre"
            if (!s.email.contains("@")) return "El email no es válido"
            if (s.password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        }
        return null
    }

    private inline fun update(block: (LoginUiState) -> LoginUiState) {
        _uiState.update(block)
    }
}