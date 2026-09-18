package com.example.data.repository

import com.example.data.db.BusinessLeadEntity
import com.example.data.db.OutreachCampaignEntity
import com.example.data.db.ZipScanReportEntity
import java.util.UUID

object ZipMarketGenerator {

    // Known preset ZIP metadata for ultra-accurate demo experience
    private val PRESET_ZIPS = mapOf(
        "97502" to ZipInfo("Central Point", "Oregon", "OR", 94000.0, 1.15),
        "78701" to ZipInfo("Austin", "Texas", "TX", 320000.0, 2.10),
        "33139" to ZipInfo("Miami Beach", "Florida", "FL", 280000.0, 1.85),
        "98101" to ZipInfo("Seattle", "Washington", "WA", 290000.0, 1.90),
        "85001" to ZipInfo("Phoenix", "Arizona", "AZ", 180000.0, 1.40),
        "60601" to ZipInfo("Chicago", "Illinois", "IL", 260000.0, 1.75),
        "90210" to ZipInfo("Beverly Hills", "California", "CA", 450000.0, 2.50)
    )

    data class ZipInfo(
        val city: String,
        val stateFull: String,
        val stateAbbr: String,
        val baseAvgRevenue: Double,
        val multiplier: Double
    )

    fun getZipInfo(zip: String): ZipInfo {
        PRESET_ZIPS[zip]?.let { return it }
        
        // Algorithmic fallbacks based on ZIP region
        val zipNum = zip.filter { it.isDigit() }.toIntOrNull() ?: 90000
        val (city, state, stateAbbr) = when (zipNum / 10000) {
            0 -> Triple("Boston Metro Area", "Massachusetts", "MA")
            1 -> Triple("New York Metro Area", "New York", "NY")
            2 -> Triple("DC / Maryland Metro", "Virginia", "VA")
            3 -> Triple("Atlanta Metro", "Georgia", "GA")
            4 -> Triple("Columbus / Midwest", "Ohio", "OH")
            5 -> Triple("Minneapolis Metro", "Minnesota", "MN")
            6 -> Triple("Chicago Area", "Illinois", "IL")
            7 -> Triple("Dallas / Houston Metro", "Texas", "TX")
            8 -> Triple("Denver / Mountain Region", "Colorado", "CO")
            9 -> Triple("Pacific West Coast", "California", "CA")
            else -> Triple("U.S. Metro Region", "United States", "US")
        }
        val mult = 1.0 + (zipNum % 100) / 100.0 * 0.8
        return ZipInfo(city, state, stateAbbr, 120000.0 * mult, mult)
    }

    data class GeneratedScanData(
        val report: ZipScanReportEntity,
        val leads: List<BusinessLeadEntity>
    )

    fun generateScanForZip(zipCode: String): GeneratedScanData {
        val zipInfo = getZipInfo(zipCode)
        val zipHash = zipCode.hashCode()
        val seed = Math.abs(zipHash)

        val categories = listOf(
            "HVAC & Plumbing",
            "Dental & Medical",
            "Restaurants & Cafes",
            "Auto Repair & Towing",
            "Legal & Financial",
            "Salons & Spas",
            "Real Estate & Title"
        )

        val nameTemplates = mapOf(
            "HVAC & Plumbing" to listOf("Apex Heating & Air", "Rogue Valley Plumbing & HVAC", "Comfort Climate Pros", "Heritage Mechanical Services", "ProFlow Plumbing"),
            "Dental & Medical" to listOf("Cascade Dental Group", "Main Street Family Medicine", "Summit Smiles & Ortho", "Pinnacle Care Clinic", "Valley Care Pediatrics"),
            "Restaurants & Cafes" to listOf("The Copper Kettle Bistro", "Artisan Bakery & Roastery", "Redwood Grill & Bar", "Pacific Harvest Kitchen", "Urban Spoon Tavern"),
            "Auto Repair & Towing" to listOf("Precision Auto Care", "Jackson Auto & Tire", "First Class Automotive", "ProTech Repair & Towing", "Express Auto Clinic"),
            "Legal & Financial" to listOf("Highland Law Group", "Pinnacle Wealth & Tax", "Frontier Title & Escrow", "Beacon Legal Counsel", "Summit Accounting Services"),
            "Salons & Spas" to listOf("Glow Aesthetics & Spa", "Velvet Hair Studio", "Pure Serenity Wellness", "Modern Edge Barbershop", "Bloom Nail & Skin Bar"),
            "Real Estate & Title" to listOf("Pacific Coast Realty", "KeyStone Property Mgmt", "Cascade Horizon Homes", "Premier Valley Real Estate", "Apex Escrow & Title")
        )

        val missingToolsPool = listOf(
            "24/7 AI Phone Receptionist (Missed Call Recovery)",
            "Instant Web Lead Auto-Textback (< 60s)",
            "AI Google Review Request & Auto-Responder",
            "Automated Online Appointment Scheduler",
            "AI No-Show & Appointment Reminder System",
            "Database Reactivation Campaign Engine",
            "AI Social Media Messenger Bot"
        )

        val leads = mutableListOf<BusinessLeadEntity>()
        var totalMissed = 0.0

        for (i in 0..6) {
            val cat = categories[i % categories.size]
            val names = nameTemplates[cat] ?: listOf("Local Business $i")
            val name = names[(seed + i) % names.size]
            
            // Calculate realistic revenue & missed potential
            val baseRev = zipInfo.baseAvgRevenue * (0.8 + ((seed + i * 17) % 50) / 100.0)
            val missedPct = when (cat) {
                "HVAC & Plumbing" -> 0.22 // High missed call cost ($1500+ avg ticket)
                "Dental & Medical" -> 0.18 // High missed appointment cost
                "Legal & Financial" -> 0.20 // High retainer value
                "Auto Repair & Towing" -> 0.16
                "Real Estate & Title" -> 0.25
                "Restaurants & Cafes" -> 0.12
                else -> 0.15
            }
            val missedRev = baseRev * missedPct
            totalMissed += missedRev

            val score = (35 + ((seed * 3 + i * 11) % 40)).coerceIn(20, 75) // Lower score = worse tech adoption = higher opportunity!
            
            val tier = when {
                missedRev >= 18000 || score < 35 -> "HOT_LEAD"
                missedRev >= 10000 -> "HIGH_UPSIDE"
                score < 55 -> "QUICK_WIN"
                else -> "NURTURE"
            }

            val numTools = 2 + (i % 3)
            val tools = missingToolsPool.shuffled(java.util.Random(seed.toLong() + i)).take(numTools)

            val streetName = listOf("Main St", "Central Ave", "Front St", "Oak St", "Pine Rd", "Commercial Way")[i % 6]
            val bldgNo = 100 + ((seed + i * 23) % 800)

            leads.add(
                BusinessLeadEntity(
                    id = "lead_${zipCode}_$i",
                    zipCode = zipCode,
                    businessName = name,
                    category = cat,
                    address = "$bldgNo $streetName, ${zipInfo.city}, ${zipInfo.stateAbbr} $zipCode",
                    phone = "(541) 555-${1000 + i * 123}",
                    website = "https://www.${name.lowercase().replace(" ", "").replace("&", "and")}.com",
                    monthlyRevenueEst = baseRev,
                    missedRevenueEst = missedRev,
                    automationScore = score,
                    qualificationTier = tier,
                    leadStage = if (i == 0) "QUALIFIED" else "NEW",
                    missingToolsCsv = tools.joinToString(", "),
                    contactName = listOf("Dave Miller", "Sarah Jenkins", "Dr. Mark Vance", "Carlos Rodriguez", "Elena Rostova", "Marcus Chen", "Rachel Adams")[i],
                    contactEmail = "owner@${name.lowercase().replace(" ", "").replace("&", "")}.com",
                    notes = "Scanned in $zipCode market analysis. Missing ${tools.firstOrNull() ?: "AI Automation"}.",
                    createdTimestamp = System.currentTimeMillis() - (i * 3600000L)
                )
            )
        }

        val report = ZipScanReportEntity(
            zipCode = zipCode,
            cityName = zipInfo.city,
            state = zipInfo.stateFull,
            scannedTimestamp = System.currentTimeMillis(),
            totalBusinessesCount = leads.size,
            totalMonthlyMissedRevenue = totalMissed,
            avgMissedRevenuePerBusiness = totalMissed / leads.size,
            topOpportunityCategory = "HVAC & Plumbing",
            automationHealthScore = (leads.map { it.automationScore }.average()).toInt(),
            highOpportunityLeadsCount = leads.count { it.qualificationTier == "HOT_LEAD" || it.qualificationTier == "HIGH_UPSIDE" }
        )

        return GeneratedScanData(report, leads)
    }

    fun generateCampaignForLead(lead: BusinessLeadEntity, type: String): OutreachCampaignEntity {
        val missedK = String.format("%.1fk", lead.missedRevenueEst / 1000.0)
        val tools = lead.missingToolsCsv.split(", ")
        val mainTool = tools.firstOrNull() ?: "24/7 AI Phone Receptionist"

        val subject = when (type) {
            "COLD_EMAIL" -> "Quick question re: estimated $${missedK}/mo missed revenue at ${lead.businessName}"
            "SMS_TEASER" -> "Hey ${lead.contactName.split(" ").firstOrNull() ?: "there"}, missed call gap at ${lead.businessName}?"
            "LINKEDIN_PITCH" -> "Automation Audit for ${lead.businessName} [${lead.zipCode}]"
            else -> "Executive Brief: Recovering $${missedK}/mo for ${lead.businessName}"
        }

        val body = when (type) {
            "COLD_EMAIL" -> """
                Hi ${lead.contactName},

                We recently ran a LocalPulse AI market audit for ${lead.category} businesses in ${lead.zipCode}.

                Based on local search volume and missed call response benchmarks, ${lead.businessName} is currently leaving an estimated $${String.format("%,.0f", lead.missedRevenueEst)}/month on the table.

                The #1 culprit? Missing ${mainTool}.

                When high-intent customers call after hours or during busy jobs, over 62% hang up and call the next business on Google.

                We build turn-key AI Voice & Text Automation for ${lead.category} that answers instantly 24/7, books appointments directly into your calendar, and recovers lost leads automatically.

                Would you be open to a 5-minute video breakdown of how we could plug this in for ${lead.businessName} this week?

                Best regards,
                LocalPulse AI Growth Agency
            """.trimIndent()

            "SMS_TEASER" -> """
                Hi ${lead.contactName}, this is LocalPulse AI. We analyzed ${lead.category} in ${lead.zipCode} and estimated ${lead.businessName} loses ~$${missedK}/mo from delayed missed-call response. Our AI receptionist answers instantly 24/7 and books appointments automatically. Mind if I send over a 2-min demo clip?
            """.trimIndent()

            "LINKEDIN_PITCH" -> """
                Hey ${lead.contactName}, noticed ${lead.businessName} is top-rated in ${lead.zipCode}! We ran an automation gap analysis for local ${lead.category} firms. Implementing $mainTool typically adds 15-22% in recovered monthly revenue without extra ad spend. Let's connect if you'd like the full report!
            """.trimIndent()

            else -> """
                EXECUTIVE BRIEF: ${lead.businessName}
                Location: ${lead.address}
                Current Tech Health Score: ${lead.automationScore}/100
                Estimated Monthly Missed Revenue: $${String.format("%,.0f", lead.missedRevenueEst)}
                Primary Gaps: ${lead.missingToolsCsv}

                RECOMMENDED AI STACK:
                1. 24/7 AI Voice & SMS Assistant
                2. Automated Google Review Booster
                3. Instant Web Booking Engine

                ESTIMATED AGENCY SETUP FEE: $2,500
                MONTHLY RETAINER: $1,500
                ESTIMATED MONTHLY ROI TO CLIENT: $${String.format("%,.0f", lead.missedRevenueEst * 0.3)} net profit recovered.
            """.trimIndent()
        }

        val roiPitch = "Recovers $${String.format("%,.0f", lead.missedRevenueEst)}/mo with 24/7 AI Automation"

        return OutreachCampaignEntity(
            id = UUID.randomUUID().toString(),
            businessId = lead.id,
            businessName = lead.businessName,
            category = lead.category,
            zipCode = lead.zipCode,
            campaignType = type,
            subjectOrHook = subject,
            bodyText = body,
            calculatedRoiPitch = roiPitch,
            status = "DRAFT",
            createdTimestamp = System.currentTimeMillis()
        )
    }
}
