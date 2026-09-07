package com.maisha.game.data

import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.HustleType
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.SkillType

/**
 * Career board: a worldwide universal catalog plus distinctive local jobs per country.
 *
 * Salaries are Kenya-shilling scale; [com.maisha.game.data.EconomyScaler] converts at hire.
 * Existing job ids are stable so saved lives and tests keep resolving.
 */
object JobPool {

    private fun job(
        id: String,
        title: String,
        education: SchoolStage,
        salary: Int,
        license: Boolean = false,
        followers: Int = 0,
        skill: SkillType? = null,
        skillLevel: Int = 0,
        military: Boolean = false
    ) = Job(
        id = id,
        title = title,
        minEducation = education,
        baseSalary = salary,
        requiresDrivingLicense = license,
        minFollowers = followers,
        skillBypass = skill,
        minSkillLevel = skillLevel,
        isMilitary = military
    )

    private val secondary = SchoolStage.SECONDARY
    private val graduated = SchoolStage.GRADUATED
    private val none = SchoolStage.NONE

    // —— Universal board (available in every country) ——

    private val universalJobs: List<Job> = listOf(
        // Informal / entry
        job("shop_attendant", "Shop Attendant", secondary, 200_000),
        job("cashier", "Cashier", secondary, 190_000),
        job("waiter", "Waiter", secondary, 185_000),
        job("bartender", "Bartender", secondary, 210_000),
        job("receptionist", "Receptionist", secondary, 230_000),
        job("call_centre_agent", "Call Centre Agent", secondary, 250_000),
        job("warehouse_worker", "Warehouse Worker", secondary, 210_000),
        job("cleaner", "Cleaner", secondary, 160_000),
        job("construction_worker", "Construction Worker", secondary, 240_000),
        job("factory_worker", "Factory Worker", secondary, 220_000),
        job("farmer", "Farmer", secondary, 175_000),
        job("salesperson", "Salesperson", secondary, 280_000),
        job("hairdresser", "Hairdresser", secondary, 230_000),
        job("hotel_clerk", "Hotel Clerk", secondary, 240_000),
        // Licensed / skilled trades
        job("security_guard", "Security Guard", secondary, 220_000),
        job("driver", "Driver", secondary, 240_000, license = true),
        job("delivery_driver", "Delivery Driver", secondary, 260_000, license = true),
        job("trucker", "Trucker", secondary, 380_000, license = true),
        job("taxi_driver", "Taxi Driver", secondary, 270_000, license = true),
        job("bus_driver", "Bus Driver", secondary, 300_000, license = true),
        job("mechanic", "Mechanic", secondary, 320_000),
        job("electrician", "Electrician", secondary, 360_000),
        job("plumber", "Plumber", secondary, 350_000),
        job("police_officer", "Police Officer", secondary, 340_000),
        job("firefighter", "Firefighter", secondary, 360_000),
        job("paramedic", "Paramedic", secondary, 400_000),
        // Degree / professional
        job("teacher", "Teacher", graduated, 480_000),
        job("librarian", "Librarian", graduated, 420_000),
        job("nurse", "Nurse", graduated, 550_000),
        job("pharmacist", "Pharmacist", graduated, 720_000),
        job("doctor", "Doctor", graduated, 1_200_000),
        job("dentist", "Dentist", graduated, 1_050_000),
        job("lawyer", "Lawyer", graduated, 1_100_000),
        job("accountant", "Accountant", graduated, 650_000),
        job("bank_teller", "Bank Teller", secondary, 310_000),
        job("banker", "Banker", graduated, 800_000),
        job("hr_officer", "HR Officer", graduated, 580_000),
        job("marketing_manager", "Marketing Manager", graduated, 780_000),
        job("project_manager", "Project Manager", graduated, 820_000),
        job("software_developer", "Software Developer", graduated, 900_000, skill = SkillType.PROGRAMMING, skillLevel = 70),
        job("journalist", "Journalist", graduated, 500_000, skill = SkillType.WRITING, skillLevel = 70),
        job("photographer", "Photographer", secondary, 360_000, skill = SkillType.WRITING, skillLevel = 40),
        job("chef", "Chef", graduated, 520_000, skill = SkillType.COOKING, skillLevel = 65),
        job("session_musician", "Session Musician", graduated, 480_000, skill = SkillType.GUITAR, skillLevel = 65),
        job("martial_arts_instructor", "Martial Arts Instructor", graduated, 450_000, skill = SkillType.MARTIAL_ARTS, skillLevel = 65),
        job("engineer", "Engineer", graduated, 1_000_000),
        job("architect", "Architect", graduated, 950_000),
        job("scientist", "Scientist", graduated, 880_000),
        job("professor", "Professor", graduated, 920_000),
        job("social_worker", "Social Worker", graduated, 500_000),
        job("civil_servant", "Civil Servant", graduated, 450_000),
        job("diplomat", "Diplomat", graduated, 980_000),
        // Audience / creator
        job("brand_ambassador", "Brand Ambassador", secondary, 700_000, followers = 50_000),
        job("professional_streamer", "Professional Streamer", secondary, 850_000, followers = 100_000),
        job("content_creator", "Content Creator", secondary, 600_000, followers = 25_000),
        // Military (education bypassed in CareerEngine)
        job("military_private", "Private", none, 280_000, military = true),
        job("military_sergeant", "Sergeant", none, 420_000, military = true),
        job("military_general", "General", none, 900_000, military = true)
    )

    // —— Local jobs (shown first on that country's career board) ——

    private val countryFlavorJobs: Map<String, List<Job>> = mapOf(
        "KE" to listOf(
            job("matatu_conductor", "Matatu Conductor", secondary, 180_000),
            job("boda_boda_rider", "Boda Boda Rider", secondary, 170_000),
            job("tea_estate_plucker", "Tea Estate Plucker", secondary, 155_000),
            job("safari_guide", "Safari Guide", secondary, 260_000),
            job("mpesa_agent", "M-Pesa Agent", secondary, 210_000)
        ),
        "NG" to listOf(
            job("danfo_conductor", "Danfo Conductor", secondary, 185_000),
            job("okada_rider", "Okada Rider", secondary, 175_000),
            job("oil_field_technician", "Oil Field Technician", graduated, 720_000),
            job("nollywood_extra", "Nollywood Extra", secondary, 190_000),
            job("market_trader_ng", "Market Trader", secondary, 200_000)
        ),
        "ZA" to listOf(
            job("minibus_taxi_conductor", "Minibus Taxi Conductor", secondary, 185_000),
            job("mine_driller", "Mine Driller", secondary, 420_000),
            job("wine_farm_worker", "Wine Farm Worker", secondary, 195_000),
            job("safari_ranger", "Safari Ranger", secondary, 280_000)
        ),
        "EG" to listOf(
            job("microbus_driver", "Microbus Driver", secondary, 175_000, license = true),
            job("cairo_tour_guide", "Cairo Tour Guide", secondary, 240_000),
            job("nile_felucca_captain", "Felucca Captain", secondary, 200_000),
            job("cotton_ginner", "Cotton Ginner", secondary, 170_000)
        ),
        "US" to listOf(
            job("ranch_hand", "Ranch Hand", secondary, 220_000),
            job("oilfield_roustabout", "Oilfield Roustabout", secondary, 410_000),
            job("fast_food_shift_lead", "Fast Food Shift Lead", secondary, 210_000),
            job("broadway_usher", "Broadway Usher", secondary, 195_000)
        ),
        "CA" to listOf(
            job("lumberjack", "Lumberjack", secondary, 340_000),
            job("oil_sands_operator", "Oil Sands Operator", secondary, 480_000),
            job("ski_instructor", "Ski Instructor", secondary, 300_000),
            job("ice_road_driver", "Ice Road Driver", secondary, 450_000, license = true)
        ),
        "GB" to listOf(
            job("black_cab_driver", "Black Cab Driver", secondary, 320_000, license = true),
            job("pub_landlord", "Pub Landlord", secondary, 310_000),
            job("nhs_porter", "NHS Porter", secondary, 230_000),
            job("north_sea_fisher", "North Sea Fisher", secondary, 360_000)
        ),
        "FR" to listOf(
            job("boulanger", "Boulanger", secondary, 280_000, skill = SkillType.COOKING, skillLevel = 40),
            job("vigneron", "Vigneron", secondary, 260_000),
            job("metro_driver_fr", "Métro Driver", secondary, 340_000, license = true),
            job("atelier_assistant", "Fashion Atelier Assistant", secondary, 270_000)
        ),
        "DE" to listOf(
            job("autoworker", "Autoworker", secondary, 380_000),
            job("brewer", "Brewer", secondary, 300_000),
            job("train_conductor_de", "Train Conductor", secondary, 360_000),
            job("baker_de", "Bäcker", secondary, 270_000, skill = SkillType.COOKING, skillLevel = 35)
        ),
        "IN" to listOf(
            job("auto_rickshaw_driver", "Auto-Rickshaw Driver", secondary, 170_000, license = true),
            job("cricket_coach", "Cricket Coach", secondary, 260_000),
            job("textile_weaver", "Textile Weaver", secondary, 165_000),
            job("it_support_in", "IT Support", graduated, 520_000, skill = SkillType.PROGRAMMING, skillLevel = 45)
        ),
        "JP" to listOf(
            job("konbini_clerk", "Konbini Clerk", secondary, 210_000),
            job("shinkansen_attendant", "Shinkansen Attendant", secondary, 340_000),
            job("sushi_apprentice", "Sushi Apprentice", secondary, 230_000, skill = SkillType.COOKING, skillLevel = 50),
            job("rice_farmer_jp", "Rice Farmer", secondary, 180_000)
        ),
        "PH" to listOf(
            job("jeepney_driver", "Jeepney Driver", secondary, 175_000, license = true),
            job("seafarer", "Seafarer", secondary, 400_000),
            job("bpo_agent", "BPO Agent", secondary, 260_000),
            job("rice_farmer_ph", "Rice Farmer", secondary, 160_000)
        ),
        "ID" to listOf(
            job("angkot_driver", "Angkot Driver", secondary, 175_000, license = true),
            job("batik_artisan", "Batik Artisan", secondary, 190_000),
            job("palm_plantation_worker", "Palm Plantation Worker", secondary, 170_000),
            job("bali_guide", "Bali Tour Guide", secondary, 240_000)
        ),
        "BR" to listOf(
            job("mototaxi_rider", "Mototaxi Rider", secondary, 180_000),
            job("coffee_harvester", "Coffee Harvester", secondary, 165_000),
            job("offshore_oil_tech", "Offshore Oil Technician", graduated, 740_000),
            job("football_youth_coach", "Football Youth Coach", secondary, 250_000)
        ),
        "MX" to listOf(
            job("maquiladora_operator", "Maquiladora Operator", secondary, 200_000),
            job("mariachi_musician", "Mariachi Musician", secondary, 230_000, skill = SkillType.GUITAR, skillLevel = 50),
            job("cancun_tour_guide", "Cancún Tour Guide", secondary, 250_000),
            job("taqueria_cook", "Taquería Cook", secondary, 190_000, skill = SkillType.COOKING, skillLevel = 35)
        )
    )

    /** All jobs across universal + flavor tiers (for illustration validation / lookup). */
    val jobs: List<Job> by lazy {
        (universalJobs + countryFlavorJobs.values.flatten()).distinctBy { it.id }
    }

    private val jobsById: Map<String, Job> by lazy { jobs.associateBy { it.id } }

    /** Local flavor first, then the worldwide board. */
    fun getJobsForCountry(countryCode: String): List<Job> =
        countryFlavorJobs[countryCode].orEmpty() + universalJobs

    fun hasCountryFlavorJobs(countryCode: String): Boolean =
        !countryFlavorJobs[countryCode].isNullOrEmpty()

    fun findById(jobId: String): Job? = jobsById[jobId.substringBefore('@')]

    data class SideHustleSpec(
        val type: HustleType,
        val basePayoutMin: Int,
        val basePayoutMax: Int,
        val minAge: Int = 18,
        val minSmarts: Int = 0,
        val requiresVehicle: Boolean = false,
        val requiresIctDegree: Boolean = false,
        val requiresGraduated: Boolean = false
    )

    private val sideHustleSpecs: List<SideHustleSpec> = listOf(
        SideHustleSpec(
            type = HustleType.RIDE_SHARE,
            basePayoutMin = 8_000,
            basePayoutMax = 28_000,
            requiresVehicle = true
        ),
        SideHustleSpec(
            type = HustleType.FREELANCE_CODING,
            basePayoutMin = 15_000,
            basePayoutMax = 45_000,
            minSmarts = 75,
            requiresIctDegree = true
        ),
        SideHustleSpec(
            type = HustleType.TUTORING,
            basePayoutMin = 6_000,
            basePayoutMax = 18_000,
            requiresGraduated = true
        ),
        SideHustleSpec(
            type = HustleType.FOOD_DELIVERY,
            basePayoutMin = 4_000,
            basePayoutMax = 14_000,
            minAge = 16
        ),
        SideHustleSpec(
            type = HustleType.RESELLING,
            basePayoutMin = 3_000,
            basePayoutMax = 12_000,
            minSmarts = 40
        ),
        SideHustleSpec(
            type = HustleType.HANDMADE_CRAFTS,
            basePayoutMin = 2_000,
            basePayoutMax = 9_000,
            minAge = 14
        ),
        SideHustleSpec(
            type = HustleType.STREAMING,
            basePayoutMin = 1_500,
            basePayoutMax = 18_000,
            minAge = 16,
            minSmarts = 35
        ),
        SideHustleSpec(
            type = HustleType.SCRIPT_CODING,
            basePayoutMin = 4_000,
            basePayoutMax = 22_000,
            minAge = 16,
            minSmarts = 60
        )
    )

    private val sideHustleByType: Map<HustleType, SideHustleSpec> =
        sideHustleSpecs.associateBy { it.type }

    fun getSideHustleSpec(type: HustleType): SideHustleSpec? = sideHustleByType[type]

    fun getAllSideHustleTypes(): List<HustleType> = sideHustleSpecs.map { it.type }

    fun meetsSideHustlePrerequisites(character: Character, type: HustleType): Boolean {
        val spec = getSideHustleSpec(type) ?: return false
        if (character.age < spec.minAge) return false
        if (spec.requiresVehicle && !ownsVehicle(character)) return false
        if (spec.requiresGraduated && character.education.stage != SchoolStage.GRADUATED) return false
        if (spec.requiresIctDegree) {
            val hasIct = character.education.courseOfStudy
                ?.equals(ICT_COURSE_NAME, ignoreCase = true) == true
            if (!hasIct && character.stats.smarts < spec.minSmarts) return false
        } else if (character.stats.smarts < spec.minSmarts) {
            return false
        }
        return true
    }

    private fun ownsVehicle(character: Character): Boolean =
        character.assets.any { it.type == AssetType.CAR || it.type == AssetType.MOTORBIKE }

    private const val ICT_COURSE_NAME = "Computer Science"
}
