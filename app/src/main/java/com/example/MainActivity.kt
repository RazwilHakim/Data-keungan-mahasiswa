package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.ui.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize our student finance ViewModel
    val viewModelFactory = FinanceViewModelFactory(application)
    val viewModel = ViewModelProvider(this, viewModelFactory)[FinanceViewModel::class.java]

    enableEdgeToEdge()
    setContent {
      val isDarkMode by viewModel.isDarkMode.collectAsState()
      MyApplicationTheme(darkTheme = isDarkMode) {
        DashboardScreen(viewModel = viewModel)
      }
    }
  }
}
