package com.example.grameen_lightenergy.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.grameen_lightenergy.data.model.PoleStatus
import com.example.grameen_lightenergy.data.model.StreetlightPole
import com.example.grameen_lightenergy.ui.viewmodel.MainUiState
import com.example.grameen_lightenergy.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToDashboard: () -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPole by viewModel.selectedPole.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Grameen Light", style = MaterialTheme.typography.titleLarge)
                        Text("Citizen-led Audit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.Default.OfflineBolt, contentDescription = "Dashboard")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MainUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SafetyBanner(poles = state.poles)
                        PoleMap(
                            poles = state.poles,
                            onPoleClick = { viewModel.selectPole(it) }
                        )
                    }
                }
                is MainUiState.Error -> {
                    Text(text = "Error: ${state.message}", modifier = Modifier.align(Alignment.Center))
                }
            }
            
            selectedPole?.let { pole ->
                ReportBottomSheet(
                    pole = pole,
                    onDismiss = { viewModel.clearSelectedPole() },
                    onReportFused = {
                        viewModel.submitComplaint(pole.id, "Fused", PoleStatus.FUSED)
                        viewModel.clearSelectedPole()
                    },
                    onReportDaytime = {
                        viewModel.submitComplaint(pole.id, "Burning in Day", PoleStatus.DAYTIME_ON)
                        viewModel.clearSelectedPole()
                    }
                )
            }
        }
    }
}

@Composable
fun SafetyBanner(poles: List<StreetlightPole>) {
    val totalPoles = poles.size.coerceAtLeast(1)
    val workingPoles = poles.count { it.status == PoleStatus.WORKING }
    val safetyPercentage = (workingPoles * 100) / totalPoles
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (safetyPercentage > 80) Color(0xFF4CAF50).copy(alpha = 0.1f) 
                             else Color(0xFFF44336).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Security, 
                contentDescription = null,
                tint = if (safetyPercentage > 80) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Village Safety Score: $safetyPercentage%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (safetyPercentage == 100) "Goal Achieved: Zero-Dark Village!" 
                           else "$workingPoles/$totalPoles lights are working perfectly.",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun PoleMap(
    poles: List<StreetlightPole>,
    onPoleClick: (StreetlightPole) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
        val width = maxWidth
        val height = maxHeight
        val roadColor = MaterialTheme.colorScheme.outlineVariant
        
        // Draw Village Roads
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Main Road
            drawLine(
                color = roadColor,
                start = Offset(0f, size.height * 0.4f),
                end = Offset(size.width, size.height * 0.6f),
                strokeWidth = 40f
            )
            // Cross Road
            drawLine(
                color = roadColor,
                start = Offset(size.width * 0.5f, 0f),
                end = Offset(size.width * 0.4f, size.height),
                strokeWidth = 35f
            )
            // Path
            drawLine(
                color = roadColor,
                start = Offset(0f, size.height * 0.8f),
                end = Offset(size.width * 0.8f, size.height * 0.1f),
                strokeWidth = 20f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )
        }

        val minLat = poles.minOfOrNull { it.lat } ?: 0.0
        val maxLat = poles.maxOfOrNull { it.lat } ?: 1.0
        val minLng = poles.minOfOrNull { it.lng } ?: 0.0
        val maxLng = poles.maxOfOrNull { it.lng } ?: 1.0
        
        val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
        val lngRange = (maxLng - minLng).coerceAtLeast(0.0001)

        Box(modifier = Modifier.fillMaxSize()) {
            poles.forEach { pole ->
                val x = ((pole.lng - minLng) / lngRange * (width.value - 120) + 60).dp
                val y = ((1.0 - (pole.lat - minLat) / latRange) * (height.value - 200) + 100).dp
                
                PoleMarker(
                    pole = pole,
                    modifier = Modifier.offset(x = x, y = y),
                    onClick = { onPoleClick(pole) }
                )
            }
        }
        
        Text(
            text = "VILLAGE MAP VIEW (Simulated Roads)",
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PoleMarker(
    pole: StreetlightPole,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = when (pole.status) {
        PoleStatus.WORKING -> Color(0xFF4CAF50)
        PoleStatus.FUSED -> Color(0xFFF44336)
        PoleStatus.DAYTIME_ON -> Color(0xFFFF9800)
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = color,
            shadowElevation = 6.dp,
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when(pole.status) {
                        PoleStatus.WORKING -> Icons.Default.Lightbulb
                        PoleStatus.FUSED -> Icons.Default.OfflineBolt
                        PoleStatus.DAYTIME_ON -> Icons.Default.WbSunny
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = pole.id.takeLast(3),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBottomSheet(
    pole: StreetlightPole,
    onDismiss: () -> Unit,
    onReportFused: () -> Unit,
    onReportDaytime: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Citizen Report", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Lamp Post: ${pole.id}", 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Fused Report Button - High Readability
            Button(
                onClick = onReportFused,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.DarkMode, null, tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Text(
                    "REPORT FUSED (DARK STREET)", 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Day Report Button - High Readability
            Button(
                onClick = onReportDaytime,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.WbSunny, null, tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Text(
                    "REPORT BURNING IN DAY", 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}
