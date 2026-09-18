package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BusinessLeadEntity
import com.example.ui.LocalPulseViewModel
import com.example.ui.components.MetricCard
import com.example.ui.components.StageBadge
import com.example.ui.components.TierBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsDashboardScreen(
    viewModel: LocalPulseViewModel,
    modifier: Modifier = Modifier
) {
    val leads by viewModel.filteredLeads.collectAsState()
    val allLeadsList by viewModel.allLeads.collectAsState()
    val currentFilter by viewModel.leadStageFilter.collectAsState()
    val searchQuery by viewModel.leadSearchQuery.collectAsState()

    var showAddLeadDialog by remember { mutableStateOf(false) }

    val totalPipelineVal = allLeadsList.sumOf { it.missedRevenueEst }
    val qualifiedCount = allLeadsList.count { it.leadStage == "QUALIFIED" || it.leadStage == "PITCHED" || it.leadStage == "INTERESTED" }

    val stageFilters = listOf(
        "ALL" to "All Leads",
        "QUALIFIED" to "Qualified",
        "PITCHED" to "Pitched",
        "INTERESTED" to "Interested",
        "WON" to "Won",
        "ARCHIVED" to "Archived"
    )

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddLeadDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Custom Lead", fontWeight = FontWeight.Bold) },
                containerColor = ElectricBlue,
                contentColor = Color.White
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Lead Qualification Engine",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Rank & score local businesses by AI automation upside",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showAddLeadDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Lead", tint = ElectricBlue)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pipeline Stats Summary
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Pipeline Opportunity",
                        value = "$${String.format("%,.0f", totalPipelineVal)}",
                        subtitle = "${allLeadsList.size} Total Businesses",
                        icon = Icons.Default.TrendingUp,
                        accentColor = EmeraldGrowth,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Active Pipeline",
                        value = "$qualifiedCount Leads",
                        subtitle = "Qualified for outreach",
                        icon = Icons.Default.FilterList,
                        accentColor = CyanPulse,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setLeadSearchQuery(it) },
                    placeholder = { Text("Search lead name, category, or ZIP...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { viewModel.setLeadSearchQuery("") }) { Icon(Icons.Default.Clear, contentDescription = null) } }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stage Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(stageFilters) { (stageKey, stageLabel) ->
                        val isSelected = currentFilter == stageKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setLeadStageFilter(stageKey) },
                            label = { Text(stageLabel, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (leads.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Slate600)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Leads Match Filter",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Try scanning a new ZIP code or adjusting search filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(leads, key = { it.id }) { lead ->
                    LeadDashboardCard(
                        lead = lead,
                        onLeadClick = { viewModel.selectLeadForModal(lead) },
                        onStageChange = { newStage -> viewModel.updateLeadStage(lead.id, newStage) },
                        onLaunchCampaign = { viewModel.generateCampaign(lead, "COLD_EMAIL") },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }

    if (showAddLeadDialog) {
        AddCustomLeadDialog(
            onDismiss = { showAddLeadDialog = false },
            onSave = { name, cat, zip, addr, phone, contact, email, missedRev ->
                viewModel.addCustomLead(name, cat, zip, addr, phone, contact, email, missedRev)
                showAddLeadDialog = false
            }
        )
    }
}

@Composable
fun LeadDashboardCard(
    lead: BusinessLeadEntity,
    onLeadClick: () -> Unit,
    onStageChange: (String) -> Unit,
    onLaunchCampaign: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedStageMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onLeadClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.businessName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${lead.category} • ZIP ${lead.zipCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    Surface(
                        modifier = Modifier.clickable { expandedStageMenu = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            StageBadge(stage = lead.leadStage)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = expandedStageMenu,
                        onDismissRequest = { expandedStageMenu = false }
                    ) {
                        listOf("QUALIFIED", "PITCHED", "INTERESTED", "WON", "ARCHIVED").forEach { stage ->
                            DropdownMenuItem(
                                text = { Text(stage, fontSize = 13.sp) },
                                onClick = {
                                    onStageChange(stage)
                                    expandedStageMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TierBadge(tier = lead.qualificationTier)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Missed: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%,.0f", lead.missedRevenueEst)}/mo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoseHot
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact Info Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${lead.contactName} (${lead.contactEmail})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onLeadClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Dossier", fontSize = 12.sp)
                }

                Button(
                    onClick = onLaunchCampaign,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Outreach Pitch", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddCustomLeadDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, cat: String, zip: String, address: String, phone: String, contact: String, email: String, missedRev: Double) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("HVAC & Plumbing") }
    var zipCode by remember { mutableStateOf("97502") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var missedRevenueInput by remember { mutableStateOf("8500") }

    val categories = listOf("HVAC & Plumbing", "Dental & Medical", "Restaurants & Cafes", "Auto Repair", "Legal & Financial", "Salons & Spas", "Real Estate")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Local Business Lead", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = { Text("ZIP Code *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Owner / Decision Maker Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = missedRevenueInput,
                    onValueChange = { missedRevenueInput = it },
                    label = { Text("Est. Monthly Missed Revenue ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val missedRev = missedRevenueInput.toDoubleOrNull() ?: 5000.0
                    onSave(businessName, category, zipCode, address, phone, contactName, contactEmail, missedRev)
                },
                enabled = businessName.isNotBlank() && zipCode.isNotBlank()
            ) {
                Text("Save Lead")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
