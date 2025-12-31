package com.latefortrain.meshhelper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.latefortrain.meshhelper.repository.MeshManager
import com.latefortrain.meshhelper.ui.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MeshViewModel(private val meshManager: MeshManager) : ViewModel() {
    val uiState: StateFlow<UiState> = meshManager.uiState

    init {
        // Start the connection loop
        viewModelScope.launch {
            while (!meshManager.isBound) {
                if (meshManager.connectToService()) {
                    break // Exit loop once binding is initiated
                }
                delay(2000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        meshManager.disconnectFromService()
    }

    fun sendText(text: String){
        meshManager.sendTextMessage(text)
    }
}