package com.example.grameen_lightenergy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.grameen_lightenergy.data.database.GrameenLightDatabase
import com.example.grameen_lightenergy.data.repository.GrameenLightRepository
import com.example.grameen_lightenergy.ui.screens.DashboardScreen
import com.example.grameen_lightenergy.ui.screens.MainScreen
import com.example.grameen_lightenergy.ui.theme.GrameenLightEnergyTheme
import com.example.grameen_lightenergy.ui.viewmodel.DashboardViewModel
import com.example.grameen_lightenergy.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = GrameenLightDatabase.getDatabase(applicationContext)
        val repository = GrameenLightRepository(
            database.streetlightPoleDao(),
            database.complaintDao()
        )
        
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            
            GrameenLightEnergyTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    GrameenLightNavHost(
                        navController = navController,
                        repository = repository,
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDarkTheme = it }
                    )
                }
            }
        }
    }
}

@Composable
fun GrameenLightNavHost(
    navController: NavHostController,
    repository: GrameenLightRepository,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModel.Factory(repository)
            )
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToDashboard = {
                    navController.navigate("dashboard")
                },
                isDarkTheme = isDarkTheme,
                onThemeToggle = { onThemeChange(!isDarkTheme) }
            )
        }
        
        composable("dashboard") {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(repository)
            )
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
