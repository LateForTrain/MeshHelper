package com.latefortrain.meshhelper.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.latefortrain.meshhelper.repository.GPSManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class GPSViewModel(
    private val gpsManager: GPSManager
) : ViewModel() {

    val location: StateFlow<Location?> = gpsManager.lastLocation

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    sealed class UiEvent {
        object RequestPermission : UiEvent()
    }

    fun requestFreshLocation(hasPermission: () -> Boolean) {   // pass permission check from UI
        viewModelScope.launch {
            if (!hasPermission()) {
                _events.send(UiEvent.RequestPermission)
                return@launch
            }

            _loading.value = true
            val freshLoc = gpsManager.getFreshLocation()
            _loading.value = false

            if (freshLoc == null) {
                _error.value = "Failed to get fresh location"
            } else {
                _error.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}