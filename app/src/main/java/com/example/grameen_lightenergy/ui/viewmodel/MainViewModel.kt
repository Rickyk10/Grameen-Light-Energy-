package com.example.grameen_lightenergy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.grameen_lightenergy.data.model.PoleStatus
import com.example.grameen_lightenergy.data.model.StreetlightPole
import com.example.grameen_lightenergy.data.repository.GrameenLightRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class MainUiState {
    data object Loading : MainUiState()
    data class Success(val poles: List<StreetlightPole>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

class MainViewModel(
    private val repository: GrameenLightRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _selectedPole = MutableStateFlow<StreetlightPole?>(null)
    val selectedPole: StateFlow<StreetlightPole?> = _selectedPole.asStateFlow()
    
    init {
        loadPoles()
    }
    
    private fun loadPoles() {
        viewModelScope.launch {
            try {
                repository.getAllPoles().collect { poles ->
                    _uiState.value = MainUiState.Success(poles)
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun selectPole(pole: StreetlightPole) {
        _selectedPole.value = pole
    }
    
    fun clearSelectedPole() {
        _selectedPole.value = null
    }
    
    fun submitComplaint(poleId: String, issueType: String, newStatus: PoleStatus) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.submitComplaint(poleId, issueType, newStatus)
            }
        }
    }
    
    class Factory(private val repository: GrameenLightRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
