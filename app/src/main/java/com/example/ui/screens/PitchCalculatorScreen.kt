package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LocalPulseViewModel
import com.example.ui.components.MetricCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PitchCalculatorScreen(
    viewModel: LocalPulseViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.calculatorSettings.collectAsState()
    val report by viewModel.currentZipReport.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var avgRetainerInput by remember(settings) { mutableStateOf(settings.avgRetainer.toInt().toString()) }
    var setupFeeInput by remember(settings) { mutableStateOf(settings.setupFee.toInt().toString()) }
    var recoveryRateSlider by remember(settings) { mutableFloatStateOf(settings.estimatedRecoveryRate.toFloat()) }

    // Computations
    val retainer = avgRetainerInput.toDoubleOrNull() ?: 1500.0
    val setup = setupFeeInput.toDoubleOrNull() ?: 2500.0
    val recoveryPct = recoveryRateSlider.toDouble()

    val avgBusinessMissed = report?.avgMissedRevenuePerBusiness ?: 18500.0
    val clientMonthlyRecovered = avgBusinessMissed * recoveryPct
    val clientAnnualRecovered = clientMonthlyRecovered * 12.0
    val clientAnnualCost = setup + (retainer * 12.0)
    val clientNetGain = clientAnnualRecovered - clientAnnualCost
    val clientRoiMultiple = if (clientAnnualCost > 0) clientAnnualRecovered / clientAnnualCost else 1.0
    val paybackMonths = if (clientMonthlyRecovered > 0) setup / clientMonthlyRecovered else 1.0

    val generatedPitchDeck = """
        LOCALPULSE AI - CONSULTANT PITCH DECK SCRIPT
        Target Market: ${report?.cityName ?: "Local Market"}, ${report?.state ?: "U.S."} (${report?.zipCode ?: "97502"})

        1. THE OPPORTUNITY GAP
        In your local market, ${report?.cityName ?: "Central Point"} businesses leave an estimated $${String.format("%,.0f", avgBusinessMissed)}/mo uncaptured due to missing 24/7 AI response, missed-call auto-textbacks, and delayed web lead follow-up.

        2. THE AI AUTOMATION SOLUTION
        We deploy a 24/7 AI Voice Receptionist + Instant SMS Booking Engine to handle 100% of missed calls and web leads in < 60 seconds.

        3. FINANCIAL RETURN ON INVESTMENT
        • Est. Monthly Recovered Revenue: $${String.format("%,.0f", clientMonthlyRecovered)}/mo
        • Est. Annual Recovered Revenue: $${String.format("%,.0f", clientAnnualRecovered)}/yr
        • Investment: $${String.format("%,.0f", setup)} upfront setup + $${String.format("%,.0f", retainer)}/mo
        • Client Payback Period: ${String.format("%.1f", paybackMonths)} months
        • NET ANNUAL ROI: ${String.format("%.1f", clientRoiMultiple)}x Return on Investment

        "We guarantee a payback period of under 60 days or we work for free until you hit positive ROI."
    """.trimIndent()

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

                Text(
                    text = "Agency ROI Pitch Calculator",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Quantify client lifetime ROI & generate close-ready pitch presentations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ROI Projections Cards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Client Annual Gain",
                        value = "+$${String.format("%,.0f", clientAnnualRecovered)}",
                        subtitle = "Net $${String.format("%,.0f", clientNetGain)} profit",
                        icon = Icons.Default.TrendingUp,
                        accentColor = EmeraldGrowth,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "ROI Multiple",
                        value = "${String.format("%.1f", clientRoiMultiple)}x",
                        subtitle = "Payback in ${String.format("%.1f", paybackMonths)} mo",
                        icon = Icons.Default.Speed,
                        accentColor = CyanPulse,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Inputs Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Agency Pricing & Recovery Controls",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = avgRetainerInput,
                                onValueChange = {
                                    avgRetainerInput = it
                                    viewModel.updateCalculatorSettings(
                                        it.toDoubleOrNull() ?: 1500.0,
                                        setupFeeInput.toDoubleOrNull() ?: 2500.0,
                                        recoveryRateSlider.toDouble(),
                                        0.15
                                    )
                                },
                                label = { Text("Monthly Retainer ($)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = setupFeeInput,
                                onValueChange = {
                                    setupFeeInput = it
                                    viewModel.updateCalculatorSettings(
                                        avgRetainerInput.toDoubleOrNull() ?: 1500.0,
                                        it.toDoubleOrNull() ?: 2500.0,
                                        recoveryRateSlider.toDouble(),
                                        0.15
                                    )
                                },
                                label = { Text("Setup Fee ($)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Estimated Revenue Recovery Rate",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "${(recoveryRateSlider * 100).toInt()}% of missed revenue",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanPulse
                            )
                        }

                        Slider(
                            value = recoveryRateSlider,
                            onValueChange = {
                                recoveryRateSlider = it
                                viewModel.updateCalculatorSettings(
                                    avgRetainerInput.toDoubleOrNull() ?: 1500.0,
                                    setupFeeInput.toDoubleOrNull() ?: 2500.0,
                                    it.toDouble(),
                                    0.15
                                )
                            },
                            valueRange = 0.10f..0.50f,
                            steps = 8
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pitch Script Box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Live Client Pitch Script",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(generatedPitchDeck))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Pitch script copied!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Pitch Deck", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = generatedPitchDeck,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        color = Slate100
                    )
                }
            }
        }
    }
}
