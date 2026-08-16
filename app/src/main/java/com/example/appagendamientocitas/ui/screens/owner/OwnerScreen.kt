package com.example.appagendamientocitas.ui.screens.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import com.example.appagendamientocitas.ui.components.AppointmentStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerScreen(
    onLogout: () -> Unit,
    viewModel: OwnerViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel del Dueño") },
                actions = { OutlinedButton(onClick = onLogout) { Text("Salir") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Citas pendientes", style = MaterialTheme.typography.labelMedium)
                        Text("${ui.pendingCount}", style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }

            item { Text("Todas las citas", style = MaterialTheme.typography.titleMedium) }

            if (ui.appointments.isEmpty()) {
                item { Text("No hay citas registradas.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(ui.appointments, key = { it.id }) { appt ->
                    AppointmentOwnerCard(
                        appointment = appt,
                        onApprove = { viewModel.approve(appt.id) },
                        onReject = { viewModel.reject(appt.id) },
                        onComplete = { viewModel.complete(appt.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppointmentOwnerCard(
    appointment: Appointment,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(appointment.service, style = MaterialTheme.typography.titleSmall)
                    Text("${appointment.clientName} · ${appointment.date} ${appointment.time}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                AppointmentStatusBadge(appointment.status)
            }

            Spacer(Modifier.height(8.dp))
            when (appointment.status) {
                AppointmentStatus.PENDING -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = false, onClick = onApprove, label = { Text("Aprobar") })
                    FilterChip(selected = false, onClick = onReject, label = { Text("Rechazar") })
                }
                AppointmentStatus.APPROVED -> Row {
                    FilterChip(selected = false, onClick = onComplete, label = { Text("Marcar completada") })
                }
                AppointmentStatus.REJECTED,
                AppointmentStatus.COMPLETED -> Unit // sin acciones
            }
        }
    }
}
