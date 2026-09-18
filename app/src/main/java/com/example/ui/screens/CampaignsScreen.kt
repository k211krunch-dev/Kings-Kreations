package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.OutreachCampaignEntity
import com.example.ui.LocalPulseViewModel
import com.example.ui.components.MetricCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsScreen(
    viewModel: LocalPulseViewModel,
    modifier: Modifier = Modifier
) {
    val campaigns by viewModel.allCampaigns.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val filteredCampaigns = campaigns.filter {
        selectedStatusFilter == "ALL" || it.status == selectedStatusFilter
    }

    val totalDrafts = campaigns.count { it.status == "DRAFT" }
    val totalSent = campaigns.count { it.status == "SENT" || it.status == "REPLIED" || it.status == "CONVERTED" }
    val totalConverted = campaigns.count { it.status == "CONVERTED" }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Automated Outreach Sequences",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Tailored multi-channel pitch campaigns for local business leads",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Performance Summary Bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Outreach Sent",
                        value = "$totalSent",
                        subtitle = "$totalConverted Converted Clients",
                        icon = Icons.Default.Send,
                        accentColor = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Draft Pitches",
                        value = "$totalDrafts",
                        subtitle = "Ready to sequence",
                        icon = Icons.Default.EditNote,
                        accentColor = CyanPulse,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Row
                val filters = listOf("ALL", "DRAFT", "SENT", "REPLIED", "CONVERTED")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { status ->
                        val isSelected = selectedStatusFilter == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatusFilter = status },
                            label = { Text(status, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filteredCampaigns.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(Icons.Default.MarkEmailUnread, contentDescription = null, modifier = Modifier.size(48.dp), tint = Slate600)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Campaigns Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Scan a ZIP code and tap 'Draft Pitch' on any business lead!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredCampaigns, key = { it.id }) { campaign ->
                    CampaignCard(
                        campaign = campaign,
                        onCopyText = {
                            clipboardManager.setText(AnnotatedString("${campaign.subjectOrHook}\n\n${campaign.bodyText}"))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Pitch text copied to clipboard!")
                            }
                        },
                        onShareIntent = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_SUBJECT, campaign.subjectOrHook)
                                putExtra(Intent.EXTRA_TEXT, campaign.bodyText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Outreach Pitch"))
                        },
                        onStatusChange = { newStatus ->
                            viewModel.updateCampaignStatus(campaign.id, newStatus)
                        },
                        onDelete = {
                            viewModel.deleteCampaign(campaign.id)
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CampaignCard(
    campaign: OutreachCampaignEntity,
    onCopyText: () -> Unit,
    onShareIntent: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedStatusMenu by remember { mutableStateOf(false) }

    val typeIcon = when (campaign.campaignType) {
        "COLD_EMAIL" -> Icons.Default.Email
        "SMS_TEASER" -> Icons.Default.Sms
        "LINKEDIN_PITCH" -> Icons.Default.Share
        else -> Icons.Default.Assignment
    }

    val typeLabel = when (campaign.campaignType) {
        "COLD_EMAIL" -> "Cold Email Sequence"
        "SMS_TEASER" -> "SMS Teaser Pitch"
        "LINKEDIN_PITCH" -> "LinkedIn InMail"
        else -> "Executive Brief"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(typeIcon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = campaign.businessName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$typeLabel • ${campaign.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    Surface(
                        modifier = Modifier.clickable { expandedStatusMenu = true },
                        color = when (campaign.status) {
                            "CONVERTED" -> EmeraldGrowth.copy(alpha = 0.2f)
                            "SENT" -> ElectricBlue.copy(alpha = 0.2f)
                            "REPLIED" -> PurpleAccent.copy(alpha = 0.2f)
                            else -> Slate700.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = campaign.status,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = when (campaign.status) {
                                    "CONVERTED" -> EmeraldGrowth
                                    "SENT" -> ElectricBlue
                                    "REPLIED" -> PurpleAccent
                                    else -> Slate300
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = expandedStatusMenu,
                        onDismissRequest = { expandedStatusMenu = false }
                    ) {
                        listOf("DRAFT", "SENT", "REPLIED", "CONVERTED").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status, fontSize = 13.sp) },
                                onClick = {
                                    onStatusChange(status)
                                    expandedStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject / Hook
            Text(
                text = "SUBJECT: ${campaign.subjectOrHook}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = CyanPulse
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body Box
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorderDark)
            ) {
                Text(
                    text = campaign.bodyText,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = Slate100
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onCopyText,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Pitch", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onShareIntent,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 12.sp)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = RoseHot)
                }
            }
        }
    }
}

