package com.latefortrain.meshhelper.viewmodel

import androidx.lifecycle.ViewModel
import com.latefortrain.meshhelper.repository.QRManager

class QRViewModel(private val qrManager: QRManager) : ViewModel() {
    val scanResult = qrManager.scanResult  // Expose for UI to collect

}