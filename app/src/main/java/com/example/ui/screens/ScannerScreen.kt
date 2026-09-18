package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.BusinessLeadEntity
import com.example.data.db.ZipScanReportEntity
import com.example.ui.LocalPulseViewModel
import com.example.ui.components.MetricCard
import com.example.ui.components.TierBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: LocalPulseViewModel,
    modifier: Modifier = Modifier
) {
    val selectedZip by viewModel.selectedZipCode.collectAsState()
    val zipInput by viewModel.zipInput.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val report by viewModel.currentZipReport.collectAsState()
    val leads by viewModel.currentZipLeads.collectAsState()

    val presetZips = listOf(
        "97502" to "Central Point, OR",
        "78701" to "Austin, TX",
        "33139" to "Miami, FL",
        "98101" to "Seattle, WA",
        "85001" to "Phoenix, AZ"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Hero Header Card with Banner Image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate800)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.localpulse_hero_banner_1786247078604),
                        contentDescription = "Market Intelligence Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay gradient for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Slate900.copy(alpha = 0.85f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = ElectricBlue.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "U.S. MARKET OPPORTUNITY SCANNER",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanPulse
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "LocalPulse AI",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ZIP Code Search & Scan Input Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Scan Any U.S. ZIP Code",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enter a 5-digit ZIP code to quantify local uncaptured revenue gaps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = zipInput,
                            onValueChange = { viewModel.updateZipInput(it) },
                            placeholder = { Text("e.g. 97502") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = ElectricBlue) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = { viewModel.scanZipCode(zipInput) },
                            enabled = !isScanning && zipInput.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SCAN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset ZIP Chips
                    Text(
                        text = "Popular Market Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presetZips) { (zip, label) ->
                            val isSelected = selectedZip == zip
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.scanZipCode(zip) },
                                label = { Text("$zip ($label)", fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Market Report Metrics
        report?.let { rep ->
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "${rep.cityName}, ${rep.state} (${rep.zipCode})",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Scanned ${rep.totalBusinessesCount} key business verticals in market area",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = EmeraldGrowth.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EmeraldGrowth, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ACTIVE MARKET", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = EmeraldGrowth)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metric Cards Grid
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetricCard(
                            title = "Total Missed / Mo",
                            value = "$${String.format("%,.0f", rep.totalMonthlyMissedRevenue)}",
                            subtitle = "Monthly revenue uncaptured",
                            icon = Icons.Default.AttachMoney,
                            accentColor = RoseHot,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Avg / Business",
                            value = "$${String.format("%,.0f", rep.avgMissedRevenuePerBusiness)}",
                            subtitle = "Per business lost revenue",
                            icon = Icons.Default.Analytics,
                            accentColor = CyanPulse,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetricCard(
                            title = "Hot Opportunities",
                            value = "${rep.highOpportunityLeadsCount} Leads",
                            subtitle = "High ROI conversion upside",
                            icon = Icons.Default.LocalFireDepartment,
                            accentColor = AmberWarning,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Tech Health Index",
                            value = "${rep.automationHealthScore} / 100",
                            subtitle = "Lower = higher AI upside",
                            icon = Icons.Default.PrecisionManufacturing,
                            accentColor = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Industry Breakdown Header
                Text(
                    text = "Top Automation Gaps by Category",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val categories = listOf(
                            "HVAC & Plumbing" to 0.88f,
                            "Dental & Medical" to 0.76f,
                            "Legal & Financial" to 0.72f,
                            "Auto Repair & Towing" to 0.65f,
                            "Real Estate" to 0.58f,
                            "Restaurants & Cafes" to 0.45f
                        )

                        categories.forEach { (cat, pct) ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(cat, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text("${(pct * 100).toInt()}% Gap", style = MaterialTheme.typography.labelSmall, color = CyanPulse)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = if (pct > 0.7f) RoseHot else ElectricBlue,
                                    trackColor = Slate700
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Identified Business Leads in ${rep.zipCode}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap any business to view opportunity dossier or generate outreach campaign",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // List of Businesses
            items(leads, key = { it.id }) { lead ->
                BusinessLeadCard(
                    lead = lead,
                    onQualify = { viewModel.selectLeadForModal(lead) },
                    onDraftCampaign = { viewModel.generateCampaign(lead, "COLD_EMAIL") },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun BusinessLeadCard(
    lead: BusinessLeadEntity,
    onQualify: () -> Unit,
    onDraftCampaign: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onQualify() },
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${lead.category} • ${lead.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TierBadge(tier = lead.qualificationTier)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = RoseHot.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, RoseHot.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = "EST. MISSED MONTHLY REVENUE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%,.0f", lead.missedRevenueEst)} / mo",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = RoseHot
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TECH SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${lead.automationScore}/100",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyanPulse
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Missing tools chips
            Text(
                text = "Primary Gaps: ${lead.missingToolsCsv}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onQualify,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Qualify Dossier", fontSize = 12.sp)
                }

                Button(
                    onClick = onDraftCampaign,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Draft Pitch", fontSize = 12.sp)
                }
            }
        }
    }
}

