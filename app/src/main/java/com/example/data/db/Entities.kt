package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zip_scan_reports")
data class ZipScanReportEntity(
    @PrimaryKey val zipCode: String,
    val cityName: String,
    val state: String,
    val scannedTimestamp: Long,
    val totalBusinessesCount: Int,
    val totalMonthlyMissedRevenue: Double,
    val avgMissedRevenuePerBusiness: Double,
    val topOpportunityCategory: String,
    val automationHealthScore: Int,
    val highOpportunityLeadsCount: Int
)

@Entity(tableName = "business_leads")
data class BusinessLeadEntity(
    @PrimaryKey val id: String,
    val zipCode: String,
    val businessName: String,
    val category: String,
    val address: String,
    val phone: String,
    val website: String,
    val monthlyRevenueEst: Double,
    val missedRevenueEst: Double,
    val automationScore: Int,
    val qualificationTier: String, // "HOT_LEAD", "HIGH_UPSIDE", "QUICK_WIN", "NURTURE"
    val leadStage: String, // "QUALIFIED", "PITCHED", "INTERESTED", "WON", "ARCHIVED"
    val missingToolsCsv: String,
    val contactName: String,
    val contactEmail: String,
    val notes: String,
    val createdTimestamp: Long
)

@Entity(tableName = "outreach_campaigns")
data class OutreachCampaignEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val businessName: String,
    val category: String,
    val zipCode: String,
    val campaignType: String, // "COLD_EMAIL", "SMS_TEASER", "LINKEDIN_PITCH", "EXECUTIVE_BRIEF"
    val subjectOrHook: String,
    val bodyText: String,
    val calculatedRoiPitch: String,
    val status: String, // "DRAFT", "QUEUED", "SENT", "REPLIED", "CONVERTED"
    val createdTimestamp: Long
)

@Entity(tableName = "calculator_settings")
data class AgencyCalculatorSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val avgRetainer: Double = 1500.0,
    val setupFee: Double = 2500.0,
    val estimatedRecoveryRate: Double = 0.25, // 25% of missed revenue
    val targetCloseRate: Double = 0.15 // 15% close rate
)
