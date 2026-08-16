package com.example.appagendamientocitas.ui.screens.owner

import com.example.appagendamientocitas.data.local.entity.Appointment

data class OwnerUiState(
    val appointments: List<Appointment> = emptyList(),
    val pendingCount: Int = 0
)