package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ZipScanReportDao {
    @Query("SELECT * FROM zip_scan_reports ORDER BY scannedTimestamp DESC")
    fun getAllScanReports(): Flow<List<ZipScanReportEntity>>

    @Query("SELECT * FROM zip_scan_reports WHERE zipCode = :zipCode LIMIT 1")
    suspend fun getScanReportByZip(zipCode: String): ZipScanReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanReport(report: ZipScanReportEntity)

    @Query("DELETE FROM zip_scan_reports WHERE zipCode = :zipCode")
    suspend fun deleteScanReport(zipCode: String)
}

@Dao
interface BusinessLeadDao {
    @Query("SELECT * FROM business_leads ORDER BY missedRevenueEst DESC")
    fun getAllLeads(): Flow<List<BusinessLeadEntity>>

    @Query("SELECT * FROM business_leads WHERE zipCode = :zipCode ORDER BY missedRevenueEst DESC")
    fun getLeadsForZip(zipCode: String): Flow<List<BusinessLeadEntity>>

    @Query("SELECT * FROM business_leads WHERE id = :id LIMIT 1")
    suspend fun getLeadById(id: String): BusinessLeadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: BusinessLeadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<BusinessLeadEntity>)

    @Query("UPDATE business_leads SET leadStage = :newStage WHERE id = :leadId")
    suspend fun updateLeadStage(leadId: String, newStage: String)

    @Query("DELETE FROM business_leads WHERE id = :leadId")
    suspend fun deleteLead(leadId: String)
}

@Dao
interface OutreachCampaignDao {
    @Query("SELECT * FROM outreach_campaigns ORDER BY createdTimestamp DESC")
    fun getAllCampaigns(): Flow<List<OutreachCampaignEntity>>

    @Query("SELECT * FROM outreach_campaigns WHERE businessId = :businessId ORDER BY createdTimestamp DESC")
    fun getCampaignsForBusiness(businessId: String): Flow<List<OutreachCampaignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: OutreachCampaignEntity)

    @Query("UPDATE outreach_campaigns SET status = :newStatus WHERE id = :campaignId")
    suspend fun updateCampaignStatus(campaignId: String, newStatus: String)

    @Query("DELETE FROM outreach_campaigns WHERE id = :campaignId")
    suspend fun deleteCampaign(campaignId: String)
}

@Dao
interface CalculatorSettingsDao {
    @Query("SELECT * FROM calculator_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AgencyCalculatorSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AgencyCalculatorSettingsEntity)
}
