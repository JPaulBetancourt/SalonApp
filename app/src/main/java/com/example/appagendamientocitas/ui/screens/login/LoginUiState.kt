package com.example.appagendamientocitas.ui.screens.login

/** Estado inmutable de la pantalla de login/registro. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",            // Solo para registro
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)