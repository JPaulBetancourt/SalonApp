package com.example.appagendamientocitas.ui.screens.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)