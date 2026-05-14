package com.example.grameen_lightenergy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.grameen_lightenergy.data.model.Complaint
import com.example.grameen_lightenergy.data.model.ComplaintStatus
import com.example.grameen_lightenergy.data.model.PoleStatus
import com.example.grameen_lightenergy.data.repository.GrameenLightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val activeComplaints: List<Complaint>,
        val energyImpact: Double,
        val safetyScore: Int
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val repository: GrameenLightRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                // Combine complaints and poles to calculate safety score
                combine(
                    repository.getActiveComplaints(),
                    repository.getAllPoles()
                ) { complaints, poles ->
                    val daytimeReports = complaints.count { 
                        it.issueType.contains("Day", ignoreCase = true) 
                    }
                    val energyImpact = daytimeReports * 0.5
                    
                    val workingPoles = poles.count { it.status == PoleStatus.WORKING }
                    val totalPoles = poles.size.coerceAtLeast(1)
                    val safetyScore = (workingPoles * 100) / totalPoles
                    
                    DashboardUiState.Success(complaints, energyImpact, safetyScore)
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun simulatePanchayatAction(complaint: Complaint) {
        viewModelScope.launch {
            val nextStatus = when (complaint.status) {
                ComplaintStatus.PENDING -> ComplaintStatus.ASSIGNED
                ComplaintStatus.ASSIGNED -> ComplaintStatus.FIXED
                ComplaintStatus.FIXED -> ComplaintStatus.FIXED
            }
            
            repository.updateComplaintStatus(complaint.complaintId, nextStatus)
            
            // If fixed, update the pole status back to WORKING
            if (nextStatus == ComplaintStatus.FIXED) {
                repository.updatePoleStatus(complaint.poleId, PoleStatus.WORKING)
            }
        }
    }
    
    class Factory(private val repository: GrameenLightRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
