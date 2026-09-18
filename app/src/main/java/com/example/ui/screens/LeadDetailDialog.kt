package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BusinessLeadEntity
import com.example.ui.components.TierBadge
import com.example.ui.theme.*

@Composable
fun LeadDetailDialog(
    lead: BusinessLeadEntity,
    onDismiss: () -> Unit,
    onStageChange: (String) -> Unit,
    onGenerateCampaign: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedCampaignType by remember { mutableStateOf("COLD_EMAIL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lead.businessName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text(
                    text = "${lead.category} • ZIP ${lead.zipCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Key Revenue Metric Box
                Surface(
                    color = RoseHot.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseHot.copy(alpha = 0.3f))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "EST. MISSED MONTHLY REVENUE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$${String.format("%,.0f", lead.missedRevenueEst)} / mo",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RoseHot
                                )
                            )
                        }

                        TierBadge(tier = lead.qualificationTier)
                    }
                }

                // Gaps list
                Text(
                    text = "Identified Automation Gaps:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                lead.missingToolsCsv.split(", ").forEach { tool ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = RoseHot, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tool, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                // Owner contact
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Decision Maker:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lead.contactName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = lead.contactEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = ElectricBlue)
                        }

                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${lead.contactEmail}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = CyanPulse)
                        }
                    }
                }

                // Stage Switcher
                Text(
                    text = "Pipeline Stage:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("QUALIFIED", "PITCHED", "INTERESTED", "WON").forEach { stage ->
                        val isSelected = lead.leadStage == stage
                        FilterChip(
                            selected = isSelected,
                            onClick = { onStageChange(stage) },
                            label = { Text(stage, fontSize = 10.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Campaign Selector
                Text(
                    text = "Outreach Sequence Type:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("COLD_EMAIL" to "Cold Email", "SMS_TEASER" to "SMS Teaser", "LINKEDIN_PITCH" to "LinkedIn").forEach { (typeKey, typeLabel) ->
                        val isSelected = selectedCampaignType == typeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCampaignType = typeKey },
                            label = { Text(typeLabel, fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGenerateCampaign(selectedCampaignType)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Campaign Pitch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
