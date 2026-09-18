package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.repository.LocalPulseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppTab(val title: String, val iconName: String) {
    SCANNER("Market Scanner", "search"),
    LEADS("Lead Pipeline", "badge"),
    CAMPAIGNS("Outreach", "send"),
    CALCULATOR("Pitch ROI", "calculator"),
    HISTORY("History", "history")
}

class LocalPulseViewModel(private val repository: LocalPulseRepository) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.SCANNER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedZipCode = MutableStateFlow("97502")
    val selectedZipCode: StateFlow<String> = _selectedZipCode.asStateFlow()

    private val _zipInput = MutableStateFlow("97502")
    val zipInput: StateFlow<String> = _zipInput.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _selectedLeadForModal = MutableStateFlow<BusinessLeadEntity?>(null)
    val selectedLeadForModal: StateFlow<BusinessLeadEntity?> = _selectedLeadForModal.asStateFlow()

    private val _leadStageFilter = MutableStateFlow("ALL")
    val leadStageFilter: StateFlow<String> = _leadStageFilter.asStateFlow()

    private val _leadSearchQuery = MutableStateFlow("")
    val leadSearchQuery: StateFlow<String> = _leadSearchQuery.asStateFlow()

    val scanReports: StateFlow<List<ZipScanReportEntity>> = repository.scanReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLeads: StateFlow<List<BusinessLeadEntity>> = repository.allLeads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCampaigns: StateFlow<List<OutreachCampaignEntity>> = repository.allCampaigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calculatorSettings: StateFlow<AgencyCalculatorSettingsEntity> = repository.calculatorSettings
        .map { it ?: AgencyCalculatorSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AgencyCalculatorSettingsEntity())

    val currentZipReport: StateFlow<ZipScanReportEntity?> = combine(scanReports, selectedZipCode) { reports, zip ->
        reports.find { it.zipCode == zip } ?: reports.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentZipLeads: StateFlow<List<BusinessLeadEntity>> = combine(allLeads, selectedZipCode) { leads, zip ->
        leads.filter { it.zipCode == zip }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredLeads: StateFlow<List<BusinessLeadEntity>> = combine(allLeads, leadStageFilter, leadSearchQuery) { leads, stage, query ->
        leads.filter { lead ->
            val stageMatch = (stage == "ALL" || lead.leadStage == stage)
            val queryMatch = query.isBlank() || 
                lead.businessName.contains(query, ignoreCase = true) ||
                lead.category.contains(query, ignoreCase = true) ||
                lead.zipCode.contains(query, ignoreCase = true) ||
                lead.contactName.contains(query, ignoreCase = true)
            stageMatch && queryMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureInitialDataSeeded()
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun updateZipInput(input: String) {
        _zipInput.value = input
    }

    fun scanZipCode(zipToScan: String) {
        val cleanZip = zipToScan.trim().filter { it.isDigit() }
        if (cleanZip.length < 3) return

        viewModelScope.launch {
            _isScanning.value = true
            _selectedZipCode.value = cleanZip
            _zipInput.value = cleanZip
            delay(800) // Brief scan animation state
            repository.scanAndSaveZip(cleanZip)
            _isScanning.value = false
        }
    }

    fun selectLeadForModal(lead: BusinessLeadEntity?) {
        _selectedLeadForModal.value = lead
    }

    fun updateLeadStage(leadId: String, newStage: String) {
        viewModelScope.launch {
            repository.updateLeadStage(leadId, newStage)
            if (_selectedLeadForModal.value?.id == leadId) {
                _selectedLeadForModal.value = _selectedLeadForModal.value?.copy(leadStage = newStage)
            }
        }
    }

    fun setLeadStageFilter(filter: String) {
        _leadStageFilter.value = filter
    }

    fun setLeadSearchQuery(query: String) {
        _leadSearchQuery.value = query
    }

    fun generateCampaign(lead: BusinessLeadEntity, campaignType: String) {
        viewModelScope.launch {
            val campaign = repository.createCampaignForLead(lead, campaignType)
            // Switch to campaigns tab
            _currentTab.value = AppTab.CAMPAIGNS
        }
    }

    fun updateCampaignStatus(campaignId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateCampaignStatus(campaignId, newStatus)
        }
    }

    fun updateCalculatorSettings(
        avgRetainer: Double,
        setupFee: Double,
        recoveryRate: Double,
        closeRate: Double
    ) {
        viewModelScope.launch {
            repository.saveCalculatorSettings(
                AgencyCalculatorSettingsEntity(
                    id = 1,
                    avgRetainer = avgRetainer,
                    setupFee = setupFee,
                    estimatedRecoveryRate = recoveryRate,
                    targetCloseRate = closeRate
                )
            )
        }
    }

    fun addCustomLead(
        businessName: String,
        category: String,
        zipCode: String,
        address: String,
        phone: String,
        contactName: String,
        contactEmail: String,
        missedRevEst: Double
    ) {
        viewModelScope.launch {
            val id = "custom_${UUID.randomUUID()}"
            val newLead = BusinessLeadEntity(
                id = id,
                zipCode = zipCode,
                businessName = businessName,
                category = category,
                address = if (address.isNotBlank()) address else "Local Address, $zipCode",
                phone = if (phone.isNotBlank()) phone else "(541) 555-0199",
                website = "https://www.${businessName.lowercase().replace(" ", "")}.com",
                monthlyRevenueEst = missedRevEst * 5.0,
                missedRevenueEst = missedRevEst,
                automationScore = 32,
                qualificationTier = "HOT_LEAD",
                leadStage = "QUALIFIED",
                missingToolsCsv = "24/7 AI Phone Receptionist, Missed Call Auto-Textback, Instant Web Lead Followup",
                contactName = if (contactName.isNotBlank()) contactName else "Business Owner",
                contactEmail = if (contactEmail.isNotBlank()) contactEmail else "owner@${businessName.lowercase().replace(" ", "")}.com",
                notes = "Manually added lead for $zipCode market outreach.",
                createdTimestamp = System.currentTimeMillis()
            )
            repository.insertLead(newLead)
        }
    }

    fun deleteScan(zipCode: String) {
        viewModelScope.launch {
            repository.deleteScanReport(zipCode)
        }
    }

    fun deleteLead(leadId: String) {
        viewModelScope.launch {
            repository.deleteLead(leadId)
        }
    }

    fun deleteCampaign(campaignId: String) {
        viewModelScope.launch {
            repository.deleteCampaign(campaignId)
        }
    }
}

class LocalPulseViewModelFactory(private val repository: LocalPulseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocalPulseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocalPulseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
