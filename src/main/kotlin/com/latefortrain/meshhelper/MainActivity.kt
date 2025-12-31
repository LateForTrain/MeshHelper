package com.latefortrain.meshhelper

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.latefortrain.meshhelper.ui.startscreen.StartScreen
import com.latefortrain.meshhelper.viewmodel.MeshViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val meshManager = (application as MainApplication).meshManager

        setContent {
            val viewModel: MeshViewModel = viewModel {
                MeshViewModel(meshManager)
            }
            StartScreen(viewModel)
        }
    }
}