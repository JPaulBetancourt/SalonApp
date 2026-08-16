package com.example.appagendamientocitas.ui.screens.client

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.appagendamientocitas.ui.components.AppointmentStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    onLogout: () -> Unit,
    viewModel: ClientViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hola, ${ui.userName}") },
                actions = { OutlinedButton(onClick = onLogout) { Text("Salir") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Solicitar cita", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = ui.service,
                            onValueChange = viewModel::onServiceChange,
                            label = { Text("Servicio (ej. Corte, Tinte, Manicura)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ui.date,
                                onValueChange = viewModel::onDateChange,
                                label = { Text("Fecha yyyy-MM-dd") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = ui.time,
                                onValueChange = {
                                    viewModel.onTimeChange(it)
                                },
                                label = { Text("Hora HH:mm") },
                                singleLine = true,
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = viewModel::checkSlot) {
                                Text("Verificar horario")
                            }
                            Button(
                                onClick = viewModel::submit,
                                enabled = !ui.isSubmitting,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (ui.isSubmitting) CircularProgressIndicator(strokeWidth = 2.dp)
                                else Text("Solicitar cita")
                            }
                        }

                        if (ui.formError != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(ui.formError!!, color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        if (ui.successMessage != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(ui.successMessage!!, color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // ---- Mis citas ----
            item {
                Text("Mis citas", style = MaterialTheme.typography.titleMedium)
            }

            if (ui.myAppointments.isEmpty()) {
                item { Text("Aún no tienes citas.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(ui.myAppointments, key = { it.id }) { appt ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(appt.service, style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f))
                                AppointmentStatusBadge(appt.status)
                            }
                            Text("${appt.date} · ${appt.time}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}