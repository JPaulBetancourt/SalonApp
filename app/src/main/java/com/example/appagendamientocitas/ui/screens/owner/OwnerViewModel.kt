package com.example.appagendamientocitas.ui.screens.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import com.example.appagendamientocitas.domain.usecase.ObserveAllAppointmentsUseCase
import com.example.appagendamientocitas.domain.usecase.UpdateAppointmentStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerViewModel @Inject constructor(
    private val observeAll: ObserveAllAppointmentsUseCase,
    private val updateStatus: UpdateAppointmentStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerUiState())
    val uiState: StateFlow<OwnerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAll().collect { list ->
                _uiState.update {
                    it.copy(
                        appointments = list,
                        pendingCount = list.count { a -> a.status == AppointmentStatus.PENDING }
                    )
                }
            }
        }
    }

    fun approve(id: Int) = setStatus(id, AppointmentStatus.APPROVED)
    fun reject(id: Int) = setStatus(id, AppointmentStatus.REJECTED)
    fun complete(id: Int) = setStatus(id, AppointmentStatus.COMPLETED)

    private fun setStatus(id: Int, status: AppointmentStatus) {
        viewModelScope.launch { updateStatus(id, status) }
    }
}