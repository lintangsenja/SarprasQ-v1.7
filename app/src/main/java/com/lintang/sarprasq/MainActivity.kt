package com.lintang.sarprasq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import android.util.Log
import com.lintang.sarprasq.ui.SarprasMainApp
import com.lintang.sarprasq.ui.theme.SarprasQTheme
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SarprasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SarprasQTheme {
                SarprasMainApp(viewModel = viewModel)
            }
        }
    }
}

