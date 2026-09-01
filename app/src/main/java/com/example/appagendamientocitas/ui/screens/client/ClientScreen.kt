package com.example.appagendamientocitas.ui.screens.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appagendamientocitas.ui.components.AppointmentStatusBadge
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClientScreen(
    onLogout: () -> Unit,
    viewModel: ClientViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    
    val services = listOf(
        "Lifting de pestañas",
        "Micropigmentación de labios",
        "Micropigmentación de cejas",
        "Laminado de cejas",
        "Cauterización de lunares",
        "Manicura tradicional",
        "Manicura semipermanente"
    )
    var expanded by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    return utcTimeMillis >= calendar.timeInMillis
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        val dateString = sdf.format(Date(millis))
                        viewModel.onDateChange(dateString)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = ui.service,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selecciona un servicio") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                services.forEach { service ->
                                    DropdownMenuItem(
                                        text = { Text(service) },
                                        onClick = {
                                            viewModel.onServiceChange(service)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ui.date,
                            onValueChange = { },
                            label = { Text("Fecha de la cita") },
                            readOnly = true,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                TextButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Calendario")
                                }
                            }
                        )
                        
                        if (ui.isSlotChecking) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Buscando horarios...", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        if (ui.availableSlots.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text("Horarios disponibles (8:00 AM - 10:00 PM):", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(8.dp))
                            
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ui.availableSlots.forEach { time ->
                                    val isSelected = ui.time == time
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onTimeChange(time) },
                                        label = { Text(time) }
                                    )
                                }
                            }
                        } else if (ui.date.isNotBlank() && !ui.isSlotChecking && ui.availableSlots.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("No hay horarios disponibles para este día o la fecha es inválida.", 
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = viewModel::submit,
                            enabled = !ui.isSubmitting && ui.time.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (ui.isSubmitting) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            else {
                                val text = if (ui.time.isNotBlank()) "Solicitar cita para las ${ui.time}" else "Selecciona un horario"
                                Text(text)
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