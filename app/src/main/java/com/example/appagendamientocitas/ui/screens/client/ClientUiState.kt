package com.example.appagendamientocitas.ui.screens.client

import com.example.appagendamientocitas.data.local.entity.Appointment

data class ClientUiState(
    val userName: String = "",
    val myAppointments: List<Appointment> = emptyList(),

    // Formulario
    val service: String = "",
    val date: String = "",   // yyyy-MM-dd
    val time: String = "",   // HH:mm
    val isSubmitting: Boolean = false,
    val formError: String? = null,
    val successMessage: String? = null
)