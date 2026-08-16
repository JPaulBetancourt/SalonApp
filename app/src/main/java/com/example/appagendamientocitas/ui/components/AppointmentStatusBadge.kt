package com.example.appagendamientocitas.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus

@Composable
fun AppointmentStatusBadge(status: AppointmentStatus) {
    val (label, color) = when (status) {
        AppointmentStatus.PENDING -> "Pendiente" to MaterialTheme.colorScheme.tertiary
        AppointmentStatus.APPROVED -> "Aprobada" to MaterialTheme.colorScheme.primary
        AppointmentStatus.REJECTED -> "Rechazada" to MaterialTheme.colorScheme.error
        AppointmentStatus.COMPLETED -> "Completada" to MaterialTheme.colorScheme.secondary
    }
    SuggestionChip(
        onClick = {},
        label = { Text(label, color = color) },
        modifier = Modifier.padding(end = 4.dp)
    )
}