package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class LocalPulseRepository(
    private val scanReportDao: ZipScanReportDao,
    private val leadDao: BusinessLeadDao,
    private val campaignDao: OutreachCampaignDao,
    private val settingsDao: CalculatorSettingsDao
) {

    val scanReports: Flow<List<ZipScanReportEntity>> = scanReportDao.getAllScanReports()
    val allLeads: Flow<List<BusinessLeadEntity>> = leadDao.getAllLeads()
    val allCampaigns: Flow<List<OutreachCampaignEntity>> = campaignDao.getAllCampaigns()
    val calculatorSettings: Flow<AgencyCalculatorSettingsEntity?> = settingsDao.getSettings()

    fun getLeadsForZip(zipCode: String): Flow<List<BusinessLeadEntity>> {
        return leadDao.getLeadsForZip(zipCode)
    }

    suspend fun ensureInitialDataSeeded() {
        val existing = scanReportDao.getAllScanReports().firstOrNull()
        if (existing.isNullOrEmpty()) {
            // Seed 97502 (Central Point, OR) and 78701 (Austin, TX)
            scanAndSaveZip("97502")
            scanAndSaveZip("78701")

            // Save default calculator settings
            settingsDao.saveSettings(AgencyCalculatorSettingsEntity())
        }
    }

    suspend fun scanAndSaveZip(zipCode: String): ZipScanReportEntity {
        val scanData = ZipMarketGenerator.generateScanForZip(zipCode)
        scanReportDao.insertScanReport(scanData.report)
        leadDao.insertLeads(scanData.leads)

        // Generate initial draft campaign for top lead
        scanData.leads.firstOrNull()?.let { topLead ->
            val draftCampaign = ZipMarketGenerator.generateCampaignForLead(topLead, "COLD_EMAIL")
            campaignDao.insertCampaign(draftCampaign)
        }

        return scanData.report
    }

    suspend fun updateLeadStage(leadId: String, newStage: String) {
        leadDao.updateLeadStage(leadId, newStage)
    }

    suspend fun insertLead(lead: BusinessLeadEntity) {
        leadDao.insertLead(lead)
    }

    suspend fun createCampaignForLead(lead: BusinessLeadEntity, campaignType: String): OutreachCampaignEntity {
        val campaign = ZipMarketGenerator.generateCampaignForLead(lead, campaignType)
        campaignDao.insertCampaign(campaign)
        return campaign
    }

    suspend fun updateCampaignStatus(campaignId: String, newStatus: String) {
        campaignDao.updateCampaignStatus(campaignId, newStatus)
    }

    suspend fun saveCalculatorSettings(settings: AgencyCalculatorSettingsEntity) {
        settingsDao.saveSettings(settings)
    }

    suspend fun deleteScanReport(zipCode: String) {
        scanReportDao.deleteScanReport(zipCode)
    }

    suspend fun deleteLead(leadId: String) {
        leadDao.deleteLead(leadId)
    }

    suspend fun deleteCampaign(campaignId: String) {
        campaignDao.deleteCampaign(campaignId)
    }
}
