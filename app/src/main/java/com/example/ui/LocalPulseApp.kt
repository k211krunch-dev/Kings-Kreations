package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPulseApp(
    viewModel: LocalPulseViewModel
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedLeadModal by viewModel.selectedLeadForModal.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(ElectricBlue, CyanPulse))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LocalPulse AI",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                },
                actions = {
                    Surface(
                        color = CyanPulse.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGrowth)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SaaS AI Engine",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanPulse
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    val icon = when (tab) {
                        AppTab.SCANNER -> Icons.Default.Radar
                        AppTab.LEADS -> Icons.Default.Badge
                        AppTab.CAMPAIGNS -> Icons.Default.Send
                        AppTab.CALCULATOR -> Icons.Default.Calculate
                        AppTab.HISTORY -> Icons.Default.History
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = { Icon(icon, contentDescription = tab.title) },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = ElectricBlue,
                            indicatorColor = ElectricBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.SCANNER -> ScannerScreen(viewModel = viewModel)
                AppTab.LEADS -> LeadsDashboardScreen(viewModel = viewModel)
                AppTab.CAMPAIGNS -> CampaignsScreen(viewModel = viewModel)
                AppTab.CALCULATOR -> PitchCalculatorScreen(viewModel = viewModel)
                AppTab.HISTORY -> HistoryScreen(viewModel = viewModel)
            }
        }
    }

    selectedLeadModal?.let { lead ->
        LeadDetailDialog(
            lead = lead,
            onDismiss = { viewModel.selectLeadForModal(null) },
            onStageChange = { newStage -> viewModel.updateLeadStage(lead.id, newStage) },
            onGenerateCampaign = { campaignType ->
                viewModel.generateCampaign(lead, campaignType)
                viewModel.selectLeadForModal(null)
            }
        )
    }
}
