package com.example.appagendamientocitas.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable
    data object Login : Screen

    @Serializable
    data object ClientHome : Screen

    @Serializable
    data object OwnerDashboard : Screen
}