package com.example.grameen_lightenergy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.grameen_lightenergy.data.model.Complaint
import com.example.grameen_lightenergy.data.model.ComplaintStatus
import com.example.grameen_lightenergy.ui.viewmodel.DashboardUiState
import com.example.grameen_lightenergy.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Citizen Audit Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DashboardUiState.Success -> {
                    DashboardContent(
                        activeComplaints = state.activeComplaints,
                        energyImpact = state.energyImpact,
                        safetyScore = state.safetyScore,
                        onUpdateStatus = { viewModel.simulatePanchayatAction(it) }
                    )
                }
                is DashboardUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    activeComplaints: List<Complaint>,
    energyImpact: Double,
    safetyScore: Int,
    onUpdateStatus: (Complaint) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnergyImpactCard(
                    energyImpact = energyImpact,
                    modifier = Modifier.weight(1f)
                )
                SafetyScoreCard(
                    score = safetyScore,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Text(
                text = "Repair Tracker (Panchayat Bridge)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (activeComplaints.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active complaints in your village",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        } else {
            items(activeComplaints) { complaint ->
                ComplaintCard(
                    complaint = complaint,
                    onActionClick = { onUpdateStatus(complaint) }
                )
            }
        }
    }
}

@Composable
fun EnergyImpactCard(energyImpact: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.EnergySavingsLeaf, null, tint = Color.White)
            Column {
                Text("Energy Saved", color = Color.White, style = MaterialTheme.typography.labelMedium)
                Text("${String.format("%.1f", energyImpact)} kWh", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SafetyScoreCard(score: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.HealthAndSafety, null, tint = Color.White)
            Column {
                Text("Safety Score", color = Color.White, style = MaterialTheme.typography.labelMedium)
                Text("$score%", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ComplaintCard(
    complaint: Complaint,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(complaint.issueType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Lamp Post: ${complaint.poleId}", style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge(status = complaint.status.name)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    text = when(complaint.status) {
                        ComplaintStatus.PENDING -> "Simulate: Assign to Technician"
                        ComplaintStatus.ASSIGNED -> "Simulate: Mark as Fixed"
                        ComplaintStatus.FIXED -> "Fixed"
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reported: ${formatDate(complaint.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, contentColor, icon) = when (status) {
        "PENDING" -> Triple(Color(0xFFFF9800), Color.White, Icons.Default.Warning)
        "ASSIGNED" -> Triple(Color(0xFF2196F3), Color.White, Icons.Default.Assignment)
        "FIXED" -> Triple(Color(0xFF4CAF50), Color.White, Icons.Default.CheckCircle)
        else -> Triple(Color.Gray, Color.White, Icons.Default.Warning)
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(status, style = MaterialTheme.typography.labelSmall, color = contentColor)
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
