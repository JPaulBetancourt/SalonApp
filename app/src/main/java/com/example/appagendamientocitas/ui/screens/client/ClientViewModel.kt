package com.example.appagendamientocitas.ui.screens.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appagendamientocitas.data.local.SessionManager
import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.domain.usecase.CreateAppointmentUseCase
import com.example.appagendamientocitas.domain.usecase.IsSlotAvailableUseCase
import com.example.appagendamientocitas.domain.usecase.ObserveMyAppointmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val observeMine: ObserveMyAppointmentsUseCase,
    private val createAppointment: CreateAppointmentUseCase,
    private val isSlotAvailable: IsSlotAvailableUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    init {
        val clientId = sessionManager.getCurrentUserId()
        _uiState.update { it.copy(userName = sessionManager.getCurrentUserName()) }

        viewModelScope.launch {
            observeMine(clientId).collect { list ->
                _uiState.update { it.copy(myAppointments = list) }
            }
        }
    }

    fun onServiceChange(v: String) = update { it.copy(service = v, formError = null) }
    fun onDateChange(v: String) = update { it.copy(date = v, formError = null, successMessage = null) }
    fun onTimeChange(v: String) = update { it.copy(time = v, formError = null, successMessage = null) }

    fun checkSlot() {
        val s = _uiState.value
        if (s.date.isBlank() || s.time.isBlank()) return
        viewModelScope.launch {
            val available = isSlotAvailable(s.date, s.time)
            _uiState.update {
                it.copy(
                    formError = if (!available) "Ese horario ya está ocupado. Elige otro." else null
                )
            }
        }
    }

    fun submit() {
        val s = _uiState.value
        val error = validate(s)
        if (error != null) {
            update { it.copy(formError = error) }
            return
        }
        update { it.copy(isSubmitting = true, formError = null) }

        viewModelScope.launch {
            val appointment = Appointment(
                clientId = sessionManager.getCurrentUserId(),
                clientName = sessionManager.getCurrentUserName(),
                service = s.service.trim(),
                date = s.date.trim(),
                time = s.time.trim()
            )
            createAppointment(appointment)
                .onSuccess {
                    update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "¡Cita solicitada! Quedó como Pendiente.",
                            service = "", date = "", time = ""
                        )
                    }
                }
                .onFailure { e ->
                    update {
                        it.copy(isSubmitting = false, formError = e.message ?: "No se pudo crear la cita")
                    }
                }
        }
    }

    private fun validate(s: ClientUiState): String? {
        if (s.service.isBlank()) return "Indica el servicio"
        if (s.date.isBlank()) return "Indica la fecha (yyyy-MM-dd)"
        if (s.time.isBlank()) return "Indica la hora (HH:mm)"
        if (!isValidDate(s.date)) return "Fecha inválida. Usa yyyy-MM-dd"
        if (!isValidTime(s.time)) return "Hora inválida. Usa HH:mm (ej. 10:30)"
        if (isPast(s.date, s.time)) return "No puedes pedir una cita en el pasado"
        return null
    }

    private fun isValidDate(d: String) = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }.parse(d)
    }.getOrNull() != null

    private fun isValidTime(t: String) = t.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$"))

    private fun isPast(date: String, time: String): Boolean {
        val dt = runCatching {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .parse("$date $time")
        }.getOrNull() ?: return false
        return dt.before(Date())
    }

    private inline fun update(block: (ClientUiState) -> ClientUiState) =
        _uiState.update(block)
}