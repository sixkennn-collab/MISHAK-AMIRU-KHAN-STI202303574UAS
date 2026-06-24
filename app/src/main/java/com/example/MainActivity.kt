package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.VeloraDashboard
import com.example.ui.screens.VeloraLogin
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBackground
import com.example.ui.viewmodel.VeloraViewModel
import com.example.ui.viewmodel.VeloraViewModelFactory

class MainActivity : ComponentActivity() {

    // Inisialisasi ViewModel lewat factory buat load repository
    private val viewModel: VeloraViewModel by viewModels {
        VeloraViewModelFactory((application as VeloraApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Biar tampilan fullscreen edge-to-edge
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var isLoggedIn by rememberSaveable { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBackground
                ) {
                    if (isLoggedIn) {
                        VeloraDashboard(viewModel = viewModel)
                    } else {
                        VeloraLogin(onLoginSuccess = { isDuress ->
                            viewModel.setCamouflageMode(isDuress)
                            isLoggedIn = true
                        })
                    }
                }
            }
        }
    }
}
