// app/src/main/java/com/maisha/game/data/CountryCatalog.kt (modified — P35 content completion pass)
package com.maisha.game.data

import com.maisha.game.data.model.Country
import com.maisha.game.data.model.CountryFinanceProfile
import com.maisha.game.data.model.CountryFinancialInstrument
import com.maisha.game.data.model.CountryFlavor
import com.maisha.game.data.model.FinancialRiskLevel
import com.maisha.game.data.model.HolidayFlavor

/**
 * Country roster + per-country flavor.
 *
 * Content research notes (P35 audit — verified sources cited in coverage doc):
 * - Exams: JP Common Test (DNC/NIAD-Japan); ZA Systemic Evaluation (DBE, post-ANA);
 *   MX secondary updated from abolished COMIPEMS (La Jornada 2025) to UNAM entrance exam.
 * - Money apps: GB Monzo (UK FCA-licensed neobank, Finextra/SplitMetrics 2024).
 * - Holidays: second entries added per national calendars (gov.za, fmino.gov.ng, etc.).
 */
object CountryCatalog {

    private val countries: List<Country> = listOf(
        Country("BR", "Brazil", "BRL", "R$"),
        Country("CA", "Canada", "CAD", "CA$"),
        Country("EG", "Egypt", "EGP", "E£"),
        Country("FR", "France", "EUR", "€"),
        Country("DE", "Germany", "EUR", "€"),
        Country("IN", "India", "INR", "₹"),
        Country("ID", "Indonesia", "IDR", "Rp"),
        Country("JP", "Japan", "JPY", "¥"),
        Country("KE", "Kenya", "KES", "KSh"),
        Country("MX", "Mexico", "MXN", "MX$"),
        Country("NG", "Nigeria", "NGN", "₦"),
        Country("PH", "Philippines", "PHP", "₱"),
        Country("ZA", "South Africa", "ZAR", "R"),
        Country("GB", "United Kingdom", "GBP", "£"),
        Country("US", "United States", "USD", "$")
    )

    private val flavors: Map<String, CountryFlavor> = listOf(
        CountryFlavor(
            countryCode = "KE",
            primaryExamName = "KCPE",
            secondaryExamName = "KCSE",
            commonTransportMode = "matatu",
            popularMoneyAppOrBank = "M-Pesa",
            greetingPhrase = "Habari",
            notableHolidays = listOf(
                HolidayFlavor("Jamhuri Day", "December republic celebrations fill streets with music and flags."),
                HolidayFlavor("Madaraka Day", "June self-rule commemorations bring communities together.")
            ),
            privateHospitalName = "Nairobi Hospital",
            secondaryGradeLabel = "Form",
            guardianHonorific = "Mama"
        ),
        CountryFlavor(
            countryCode = "NG",
            primaryExamName = "BECE",
            secondaryExamName = "WAEC",
            commonTransportMode = "danfo",
            popularMoneyAppOrBank = "OPay",
            greetingPhrase = "Sannu",
            notableHolidays = listOf(
                HolidayFlavor("Independence Day", "October first parades and green-white celebrations nationwide."),
                HolidayFlavor("Democracy Day", "June twelfth commemorations honour Nigeria's return to democratic rule.")
            ),
            privateHospitalName = "Lagoon Hospitals",
            secondaryGradeLabel = "Form",
            guardianHonorific = "Mama"
        ),
        CountryFlavor(
            countryCode = "ZA",
            primaryExamName = "Systemic Evaluation",
            secondaryExamName = "NSC Matric",
            commonTransportMode = "minibus taxi",
            popularMoneyAppOrBank = "SnapScan",
            greetingPhrase = "Sawubona",
            notableHolidays = listOf(
                HolidayFlavor("Heritage Day", "September braais and cultural dress honour the nation's diversity."),
                HolidayFlavor("Freedom Day", "April twenty-seventh marks South Africa's first democratic elections in 1994.")
            ),
            privateHospitalName = "Netcare Hospital",
            secondaryGradeLabel = "Form",
            guardianHonorific = "Mom"
        ),
        CountryFlavor(
            countryCode = "EG",
            primaryExamName = "Thanaweya Amma Prep",
            secondaryExamName = "Thanaweya Amma",
            commonTransportMode = "metro",
            popularMoneyAppOrBank = "Fawry",
            greetingPhrase = "Ahlan",
            notableHolidays = listOf(
                HolidayFlavor("Revolution Day", "July commemorations mark the 1952 republic with public ceremonies."),
                HolidayFlavor("Sinai Liberation Day", "April twenty-fifth parades recall the 1982 return of the Sinai Peninsula.")
            ),
            privateHospitalName = "Cleopatra Hospital",
            secondaryGradeLabel = "Year",
            guardianHonorific = "Mama"
        ),
        CountryFlavor(
            countryCode = "US",
            primaryExamName = "State Assessment",
            secondaryExamName = "SAT",
            commonTransportMode = "school bus",
            popularMoneyAppOrBank = "Venmo",
            greetingPhrase = "Hey",
            notableHolidays = listOf(
                HolidayFlavor("Thanksgiving", "November gatherings centre on gratitude, turkey, and family tables."),
                HolidayFlavor("Independence Day", "July fourth fireworks and barbecues mark the nation's founding.")
            ),
            privateHospitalName = "Mayo Clinic",
            secondaryGradeLabel = "Grade",
            guardianHonorific = "Mom"
        ),
        CountryFlavor(
            countryCode = "CA",
            primaryExamName = "Provincial Assessment",
            secondaryExamName = "Diploma Exam",
            commonTransportMode = "school bus",
            popularMoneyAppOrBank = "Interac",
            greetingPhrase = "Hello",
            notableHolidays = listOf(
                HolidayFlavor("Canada Day", "July first fireworks and maple flags mark confederation."),
                HolidayFlavor("Thanksgiving", "October long-weekend feasts gather families before winter sets in.")
            ),
            privateHospitalName = "Toronto General Hospital",
            secondaryGradeLabel = "Grade",
            guardianHonorific = "Mom"
        ),
        CountryFlavor(
            countryCode = "GB",
            primaryExamName = "GCSE",
            secondaryExamName = "A-Levels",
            commonTransportMode = "bus",
            popularMoneyAppOrBank = "Monzo",
            greetingPhrase = "Hello",
            notableHolidays = listOf(
                HolidayFlavor("Guy Fawkes Night", "November bonfires and fireworks light up neighbourhoods."),
                HolidayFlavor("Remembrance Day", "November eleventh silences honour those who served in wartime.")
            ),
            privateHospitalName = "The London Clinic",
            secondaryGradeLabel = "Year",
            guardianHonorific = "Mum"
        ),
        CountryFlavor(
            countryCode = "FR",
            primaryExamName = "Brevet",
            secondaryExamName = "Baccalauréat",
            commonTransportMode = "métro",
            popularMoneyAppOrBank = "Lydia",
            greetingPhrase = "Bonjour",
            notableHolidays = listOf(
                HolidayFlavor("Bastille Day", "July fourteenth parades and fireworks celebrate the republic."),
                HolidayFlavor("Labour Day", "May first marches and spring outings mark workers' solidarity.")
            ),
            privateHospitalName = "Hôpital Américain de Paris",
            secondaryGradeLabel = "Year",
            guardianHonorific = "Maman"
        ),
        CountryFlavor(
            countryCode = "DE",
            primaryExamName = "Hauptschulabschluss",
            secondaryExamName = "Abitur",
            commonTransportMode = "U-Bahn",
            popularMoneyAppOrBank = "Giropay",
            greetingPhrase = "Hallo",
            notableHolidays = listOf(
                HolidayFlavor("Unity Day", "October third marks German reunification with civic events."),
                HolidayFlavor("Labour Day", "May first rallies and street festivals celebrate workers across Germany.")
            ),
            privateHospitalName = "Helios Klinikum Berlin",
            secondaryGradeLabel = "Year",
            guardianHonorific = "Mama"
        ),
        CountryFlavor(
            countryCode = "IN",
            primaryExamName = "CBSE Class 10 Board",
            secondaryExamName = "CBSE Class 12 Board",
            commonTransportMode = "auto-rickshaw",
            popularMoneyAppOrBank = "UPI",
            greetingPhrase = "Namaste",
            notableHolidays = listOf(
                HolidayFlavor("Diwali", "Festival of lights — diyas, sweets, and family visits fill the week."),
                HolidayFlavor("Holi", "Spring colour festivals splash streets with powder and laughter.")
            ),
            privateHospitalName = "Apollo Hospital",
            secondaryGradeLabel = "Class",
            guardianHonorific = "Mum"
        ),
        CountryFlavor(
            countryCode = "JP",
            primaryExamName = "Junior High Entrance Exam",
            secondaryExamName = "Common Test for University Admissions",
            commonTransportMode = "train",
            popularMoneyAppOrBank = "PayPay",
            greetingPhrase = "Konnichiwa",
            notableHolidays = listOf(
                HolidayFlavor("Obon", "Mid-August dances and lanterns honour ancestors and homecomings."),
                HolidayFlavor("New Year's Day", "January shrine visits and family meals open the year with hope.")
            ),
            privateHospitalName = "St. Luke's International Hospital",
            secondaryGradeLabel = "Year",
            guardianHonorific = "Okaa-san"
        ),
        CountryFlavor(
            countryCode = "PH",
            primaryExamName = "NAT",
            secondaryExamName = "NAT Grade 12",
            commonTransportMode = "jeepney",
            popularMoneyAppOrBank = "GCash",
            greetingPhrase = "Kumusta",
            notableHolidays = listOf(
                HolidayFlavor("Independence Day", "June twelfth flag ceremonies and street parades nationwide."),
                HolidayFlavor("Rizal Day", "December thirtieth honours national hero José Rizal with wreath-laying.")
            ),
            privateHospitalName = "St. Luke's Medical Center",
            secondaryGradeLabel = "Grade",
            guardianHonorific = "Nanay"
        ),
        CountryFlavor(
            countryCode = "ID",
            primaryExamName = "UN SMP",
            secondaryExamName = "UN SMA",
            commonTransportMode = "angkot",
            popularMoneyAppOrBank = "GoPay",
            greetingPhrase = "Halo",
            notableHolidays = listOf(
                HolidayFlavor("Independence Day", "August seventeenth competitions and flag-raising at schools."),
                HolidayFlavor("Kartini Day", "April twenty-first celebrates women's emancipation with school ceremonies.")
            ),
            privateHospitalName = "Siloam Hospital",
            secondaryGradeLabel = "Year",
            guardianHonorific = "Ibu"
        ),
        CountryFlavor(
            countryCode = "BR",
            primaryExamName = "SAEB",
            secondaryExamName = "ENEM",
            commonTransportMode = "ônibus",
            popularMoneyAppOrBank = "Pix",
            greetingPhrase = "Olá",
            notableHolidays = listOf(
                HolidayFlavor("Carnival", "Pre-Lent samba, costumes, and street parties sweep the cities."),
                HolidayFlavor("Independence Day", "September seventh parades and green-yellow flags mark nationhood.")
            ),
            privateHospitalName = "Hospital Albert Einstein",
            secondaryGradeLabel = "Year",
            guardianHonorific = "Mãe"
        ),
        CountryFlavor(
            countryCode = "MX",
            primaryExamName = "PLANEA",
            secondaryExamName = "UNAM Entrance Exam",
            commonTransportMode = "metro",
            popularMoneyAppOrBank = "SPEI",
            greetingPhrase = "Hola",
            notableHolidays = listOf(
                HolidayFlavor("Independence Day", "September sixteenth el grito re-enactments and plaza festivities."),
                HolidayFlavor("Día de los Muertos", "November altars of marigolds and pan de muerto welcome returning souls.")
            ),
            privateHospitalName = "Centro Médico ABC",
            secondaryGradeLabel = "Year",
            guardianHonorific = "Mamá"
        )
    ).associateBy { it.countryCode }

    private val genericFlavor = CountryFlavor(
        countryCode = "XX",
        primaryExamName = "Primary Leaving Exam",
        secondaryExamName = "Secondary Certificate Exam",
        commonTransportMode = "public transport",
        popularMoneyAppOrBank = "mobile banking",
        greetingPhrase = "Hello",
        notableHolidays = emptyList(),
        privateHospitalName = "Private Hospital",
        secondaryGradeLabel = "Year",
        guardianHonorific = "Mom"
    )

    private val financeProfiles: Map<String, CountryFinanceProfile> = listOf(
        CountryFinanceProfile(
            countryCode = "KE",
            primaryInstrument = CountryFinancialInstrument(
                id = "sacco_chama_ke",
                name = "SACCO Share Capital & Chama",
                localName = "SACCO / Chama",
                description = "Cooperative savings society providing regular annual dividend shares and access to member development loans.",
                annualYieldPercent = 0.115,
                minimumDepositKenya = 5_000,
                riskLevel = FinancialRiskLevel.LOW,
                allowsOverdraft = true,
                overdraftLimitKenya = 15_000
            ),
            familyObligationFraction = 0.08,
            mobileOverdraftFacility = true,
            mobileOverdraftLimitKenya = 15_000
        ),
        CountryFinanceProfile(
            countryCode = "NG",
            primaryInstrument = CountryFinancialInstrument(
                id = "tbills_fd_ng",
                name = "Treasury Bills & High-Yield Fixed Deposit",
                localName = "T-Bills & Fixed Deposit",
                description = "Sovereign treasury bills and commercial bank fixed deposits offering high nominal yield against inflation.",
                annualYieldPercent = 0.140,
                minimumDepositKenya = 20_000,
                riskLevel = FinancialRiskLevel.LOW
            ),
            familyObligationFraction = 0.12,
            mobileOverdraftFacility = false
        ),
        CountryFinanceProfile(
            countryCode = "ZA",
            primaryInstrument = CountryFinancialInstrument(
                id = "stokvel_fund_za",
                name = "Stokvel Community Investment Club",
                localName = "Stokvel",
                description = "Traditional pooled community savings scheme with rotation payouts and disciplined group dividends.",
                annualYieldPercent = 0.095,
                minimumDepositKenya = 5_000,
                riskLevel = FinancialRiskLevel.GUARANTEED
            ),
            familyObligationFraction = 0.06
        ),
        CountryFinanceProfile(
            countryCode = "EG",
            primaryInstrument = CountryFinancialInstrument(
                id = "national_cd_eg",
                name = "National High-Yield Certificate of Deposit",
                localName = "Shahadat El-Estesmar",
                description = "Public bank savings certificates guaranteeing competitive interest rates.",
                annualYieldPercent = 0.160,
                minimumDepositKenya = 10_000,
                riskLevel = FinancialRiskLevel.GUARANTEED
            ),
            familyObligationFraction = 0.07
        ),
        CountryFinanceProfile(
            countryCode = "US",
            primaryInstrument = CountryFinancialInstrument(
                id = "retirement_401k_us",
                name = "401(k) & Roth IRA Retirement Index",
                localName = "401(k) / Roth IRA",
                description = "Tax-sheltered retirement account compounding across broad market index equities.",
                annualYieldPercent = 0.085,
                minimumDepositKenya = 2_000,
                riskLevel = FinancialRiskLevel.MODERATE,
                isRetirementTaxAdvantaged = true
            ),
            familyObligationFraction = 0.02
        ),
        CountryFinanceProfile(
            countryCode = "CA",
            primaryInstrument = CountryFinancialInstrument(
                id = "tfsa_rrsp_ca",
                name = "TFSA & RRSP Index Portfolio",
                localName = "TFSA / RRSP",
                description = "Tax-Free Savings Account and Registered Retirement Savings Plan compounding tax-sheltered wealth.",
                annualYieldPercent = 0.080,
                minimumDepositKenya = 2_000,
                riskLevel = FinancialRiskLevel.MODERATE,
                isRetirementTaxAdvantaged = true
            ),
            familyObligationFraction = 0.02
        ),
        CountryFinanceProfile(
            countryCode = "GB",
            primaryInstrument = CountryFinancialInstrument(
                id = "stocks_isa_gb",
                name = "Stocks & Shares ISA",
                localName = "Stocks & Shares ISA",
                description = "UK Individual Savings Account protecting annual investment gains from capital gains and dividend taxes.",
                annualYieldPercent = 0.075,
                minimumDepositKenya = 2_000,
                riskLevel = FinancialRiskLevel.MODERATE,
                isRetirementTaxAdvantaged = true
            ),
            familyObligationFraction = 0.02
        ),
        CountryFinanceProfile(
            countryCode = "FR",
            primaryInstrument = CountryFinancialInstrument(
                id = "livret_a_fr",
                name = "Livret A & Assurance-Vie",
                localName = "Livret A",
                description = "Guaranteed tax-free state-regulated savings account ensuring steady capital preservation.",
                annualYieldPercent = 0.040,
                minimumDepositKenya = 1_000,
                riskLevel = FinancialRiskLevel.GUARANTEED,
                isRetirementTaxAdvantaged = true
            ),
            familyObligationFraction = 0.03
        ),
        CountryFinanceProfile(
            countryCode = "DE",
            primaryInstrument = CountryFinancialInstrument(
                id = "festgeld_de",
                name = "Festgeld & Bausparvertrag",
                localName = "Festgeld / Bausparvertrag",
                description = "Secured fixed-term deposit and building society savings agreement for disciplined capital security.",
                annualYieldPercent = 0.045,
                minimumDepositKenya = 2_000,
                riskLevel = FinancialRiskLevel.GUARANTEED
            ),
            familyObligationFraction = 0.02
        ),
        CountryFinanceProfile(
            countryCode = "IN",
            primaryInstrument = CountryFinancialInstrument(
                id = "ppf_fd_in",
                name = "Public Provident Fund & Bank FD",
                localName = "PPF & Bank FD",
                description = "Government-backed long-term provident savings and fixed deposit schemes with tax deductions.",
                annualYieldPercent = 0.078,
                minimumDepositKenya = 4_000,
                riskLevel = FinancialRiskLevel.GUARANTEED,
                isRetirementTaxAdvantaged = true
            ),
            familyObligationFraction = 0.09
        ),
        CountryFinanceProfile(
            countryCode = "JP",
            primaryInstrument = CountryFinancialInstrument(
                id = "nisa_jp",
                name = "NISA & Postal Savings",
                localName = "NISA & Yucho",
                description = "Nippon Individual Savings Account providing tax exemption on capital gains and steady postal deposits.",
                annualYieldPercent = 0.060,
                minimumDepositKenya = 4_000,
                riskLevel = FinancialRiskLevel.LOW,
                isRetirementTaxAdvantaged = true
            ),
            familyObligationFraction = 0.03
        ),
        CountryFinanceProfile(
            countryCode = "PH",
            primaryInstrument = CountryFinancialInstrument(
                id = "pagibig_mp2_ph",
                name = "Pag-IBIG Modified MP2 Fund",
                localName = "Pag-IBIG MP2",
                description = "High-yielding government voluntary savings program offering tax-free annual dividends.",
                annualYieldPercent = 0.072,
                minimumDepositKenya = 2_000,
                riskLevel = FinancialRiskLevel.GUARANTEED
            ),
            familyObligationFraction = 0.10
        ),
        CountryFinanceProfile(
            countryCode = "ID",
            primaryInstrument = CountryFinancialInstrument(
                id = "deposito_arisan_id",
                name = "Deposito Berjangka & Arisan",
                localName = "Deposito & Arisan",
                description = "Commercial bank fixed-term deposit combined with community rotating credit network.",
                annualYieldPercent = 0.065,
                minimumDepositKenya = 4_000,
                riskLevel = FinancialRiskLevel.LOW
            ),
            familyObligationFraction = 0.08
        ),
        CountryFinanceProfile(
            countryCode = "BR",
            primaryInstrument = CountryFinancialInstrument(
                id = "tesouro_selic_br",
                name = "Tesouro Direto & CDB",
                localName = "Tesouro Selic / CDB",
                description = "National treasury bond linked to basic interest rate offering robust inflation-indexed returns.",
                annualYieldPercent = 0.110,
                minimumDepositKenya = 2_000,
                riskLevel = FinancialRiskLevel.LOW
            ),
            familyObligationFraction = 0.05
        ),
        CountryFinanceProfile(
            countryCode = "MX",
            primaryInstrument = CountryFinancialInstrument(
                id = "cetes_directo_mx",
                name = "Cetes Directo Treasury",
                localName = "Cetes Directo",
                description = "Federal treasury certificates backed by Banco de México offering accessible, risk-free yields.",
                annualYieldPercent = 0.105,
                minimumDepositKenya = 2_000,
                riskLevel = FinancialRiskLevel.GUARANTEED
            ),
            familyObligationFraction = 0.05
        )
    ).associateBy { it.countryCode }

    private val genericFinanceProfile = CountryFinanceProfile(
        countryCode = "XX",
        primaryInstrument = CountryFinancialInstrument(
            id = "index_savings_xx",
            name = "High-Yield Savings & Index Fund",
            localName = "Treasury & Index Fund",
            description = "Diversified sovereign bonds and index fund offering steady long-term appreciation.",
            annualYieldPercent = 0.060,
            minimumDepositKenya = 2_000,
            riskLevel = FinancialRiskLevel.LOW
        ),
        familyObligationFraction = 0.03
    )

    private val unknownCountry = Country("XX", "International", "USD", "$")

    private val byCode: Map<String, Country> = countries.associateBy { it.code }

    fun all(): List<Country> = countries

    /** Unknown codes resolve to a neutral international sentinel — not Kenya. */
    fun getCountry(code: String): Country = byCode[code] ?: unknownCountry

    fun flavorFor(countryCode: String): CountryFlavor =
        flavors[countryCode] ?: genericFlavor

    fun financeProfileFor(countryCode: String): CountryFinanceProfile =
        financeProfiles[countryCode] ?: genericFinanceProfile

    fun allFinanceProfiles(): List<CountryFinanceProfile> = financeProfiles.values.toList()

    fun hasResearchedFlavor(countryCode: String): Boolean = countryCode in flavors

    fun hasHolidayEvents(countryCode: String): Boolean =
        flavorFor(countryCode).notableHolidays.isNotEmpty()

    fun search(query: String): List<Country> {
        if (query.isBlank()) return countries
        val q = query.trim().lowercase()
        return countries.filter {
            it.displayName.lowercase().contains(q) || it.code.lowercase().contains(q)
        }
    }
}
