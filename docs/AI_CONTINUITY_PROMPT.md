# Maisha / Life-simulator — AI Continuity Prompt

> **Purpose:** Read this document at the start of any new session so the assistant or developer understands **what Maisha is**, **what has already been built**, **what just shipped**, **what is still missing**, and **how to work in this repo without undoing progress**.
>
> **Last updated:** 2026-11-24 (15-Country Authentic Financial Instruments, Remittances, Google Play Billing v7, Social Ecosystem & Memory, Routine Micro-Dilemmas, Chapter Titles, BitLife/Another Life Dashboards)  
> **Repository:** `https://github.com/Dr-Rank1/Life-simulator` (`origin/main`)  
> **Package:** `com.maisha.game`

---

## 1. One-Paragraph Product Summary

**Maisha** (Swahili for *life*) is a single-player Android **life simulation** game in the BitLife / Age Sim genre. The player creates a character, ages **one year at a time**, and makes active choices across education, career, relationships, finances, crime, health, politics, leisure, and legacy. The game is **offline-first**: narrative content lives in 19 JSON packs under `app/src/main/assets/data/events/`, and game rules live in pure Kotlin **domain engines**. It targets mid-range phones (**~360dp width**, **minSdk 26**, **targetSdk 35**) while supporting adaptive layouts (`LeftNavRail`) for tablets and foldables. The product is natively localized for **15 countries** (Kenya, Nigeria, South Africa, Egypt, USA, Canada, UK, France, Germany, India, Japan, Philippines, Indonesia, Brazil, Mexico) with real currencies, names, exam systems, transport modes, localized financial instruments, and money apps.

**Design north star:** Every year should feel like there is something meaningful to *do* (interactive systems, NPC actions, career agency, asset management), not only a passive random event card.

---

## 2. Rules of Engagement for AI Assistants

1. **Read code before rewriting:** Prefer extending existing engines, viewmodels, and screens over inventing parallel systems.
2. **Domain engines stay pure:** No `Context`, `Bitmap`, `View`, `Room`, `Compose`, or Android framework classes inside `app/src/main/java/com/maisha/game/domain/`. Engines take immutable models and return updated copies.
3. **UI reads StateFlow only:** ViewModels (`@HiltViewModel`) own orchestration; Compose screens observe `StateFlow` and send user intents to ViewModel methods.
4. **Small, focused diffs:** Match existing Compose patterns, Maisha theme tokens (`LifeGreen`, `PaperWhite`, `CanvasBg`, `StatTokens`), and `strings.xml` for user-facing copy.
5. **Events are content + gates:** New JSON events must be wired in `EventRepository.loadAllFromAssets` and registered in `GlobalContentCoverageTest`.
6. **School NPCs are not family:** Classmates/teachers live on `education.schoolPeople` (`SchoolPerson`), not `Character.family`.
7. **Git:** Commit and push **only when asked**. Do not force-push, amend commits, or change git config without instructions.
8. **Documentation Location:** All project documentation lives in the root `docs/` folder (`README.md`, `GAME_OVERVIEW.md`, `GEMINI.md`, `AI_CONTINUITY_PROMPT.md`, `EVENTS_SYSTEM.md`, `PRIVACY_POLICY.md`). Keep them synchronized.
9. **Country neutrality:** Prefer country catalogs + `{placeholders}` over hardcoding Kenya or any specific country as the default experience.
10. **Prefer player agency:** When asked for "more things to do," prefer interactive actions, bottom sheets, NPC mechanics, and UI surfaces over dumping static event text.

---

## 3. Tech Stack & Useful Commands

| Technology | Role |
|---|---|
| **Kotlin 2.0+** | App programming language (Java 17 bytecode) |
| **Jetpack Compose + Material 3** | Modern declarative UI, spring physics, adaptive layouts |
| **MVVM + Dagger Hilt** | Dependency injection & ViewModel lifecycle management |
| **Room Database** | Multi-slot character saves (versioned JSON state blobs) |
| **DataStore Preferences** | App settings (sound, haptics, language, onboarding, tips) |
| **Kotlinx Serialization** | Event JSON parsing & deep state serialization |
| **AndroidX WorkManager** | Contextual daily retention reminders |
| **Google Mobile Ads (AdMob)** | Interstitial on age-ups, banner on slot picker, rewarded Second Wind |
| **Google Play Billing v7** | In-app store (`billing-ktx`), country-scaled vouchers, permanent ad removal & creator pass |
| **Google Play Games Services v2** | Cloud save snapshots (`SnapshotsClient`) & global leaderboards |
| **JUnit 4 & Kotlin Test** | Domain unit tests under `app/src/test/` |

**Useful Gradle Commands:**

```bash
# Build debug APK
./gradlew assembleDebug

# Install debug on connected device/emulator
./gradlew :app:installDebug

# Run all domain and content unit tests
./gradlew testDebugUnitTest

# Run specific engine test
./gradlew :app:testDebugUnitTest --tests "com.maisha.game.domain.EducationEngineTest"
```

---

## 4. Architecture & Data Flow

```
Compose Screens / Tabs
        ↓
  ViewModels (StateFlow)   e.g. LifeViewModel, CharacterCreationViewModel
        ↓
  GameEngine (central yearly tick coordinator + facades)
        ↓
  Feature Engines (Education, Career, Relationship, Finance, Health, Crime, ...)
        ↓
  Repositories (Room / DataStore / EventRepository + JSON assets)
```

### The Yearly Age-Up Loop
1. Player taps the tactile **3D AgeUpButton** on the Life tab.
2. `GameEngine.advanceYear` increments age and ticks sub-engines in sequence:
   - `EducationEngine.tickSchoolYear`: School stage, grades, classmate/teacher bond decay, club ranks.
   - `CareerEngine.tick`: Job performance, promotion/raise evaluations, PIPs, part-time pay, side hustle yields, fame decay.
   - `FinanceEngine.tick`: Tax liabilities, living expenses, mortgage amortization, vehicle upkeep, student loan interest, crypto yields.
   - `RelationshipEngine.tick`: Annual bond decay, family aging, partner status, pet health and mortality.
   - `HealthEngine.tick`: Age-based physical deterioration, chronic condition progress, sickness rolls.
   - `CrimeEngine.tick`: Parole board reviews, gang standing, sentence counters.
   - `MortalityEngine.checkMortality`: Age and health-based natural and accidental mortality odds.
3. `EventRepository.getEligibleEvents` filters JSON narrative events by age, career status, education gates, and country.
4. If an event fires, the player sees an `EventDialogStyled` modal with meaningful choices.
5. Choice outcomes apply immediate deltas (`statEffects`, GPA, relationships, money, crime flags, relocation).
6. The event log in `LifeScreen` updates with connected vertical milestone tracks and auto-scrolls to the top.

---

## 5. Project Layout Map

```
app/src/main/java/com/maisha/game/
  domain/                  # Pure game engines (18 engines, pure Kotlin)
  data/                    # Catalogs (Country, Job, Name), EventRepository
  data/model/              # Character, Person, EducationState, Career, Finance, Asset, Crime...
  data/events/             # EventRepository implementation
  data/local/              # Room database, DAOs, TypeConverters
  ui/main/                 # LifeScreen, ActionsScreen, FamilyScreen, CareerScreen, AssetsScreen, LifeViewModel
  ui/charactercreation/     # BitLife-style single-screen New Life creation flow
  ui/avatar/               # ProceduralAvatarPortrait (Canvas engine), ExpressionResolver
  ui/components/           # 3D AgeUpButton, DrillDownRow, InsetCardContainer, LeftNavRail, sheets
  ui/theme/                # Colors (CanvasBg, LifeGreen, StatTokens), Type, Shape
  ui/navigation/           # MaishaNavHost
  di/                      # Hilt modules (AppModule, DatabaseModule)
  notifications/          # WorkManager daily retention reminders
  feedback/                # FeedbackManager (44.1kHz audio synthesis + multi-waveform haptics)
  ads/                     # AdMob wrappers (AdUnitConfig)
  util/                    # Extensions, formatMoney, clampStat

app/src/main/assets/data/events/  # 18 JSON narrative event packs (multi-year story arcs, country flavor)
app/src/test/java/com/maisha/game/ # Domain unit tests & content coverage tests
docs/                              # Centralized project documentation
```

---

## 6. Core UI & Navigation System

### Main Navigation (`MainTab`)
The game layout adapts dynamically: compact phones use bottom navigation; tablets, foldables, and landscape mode automatically switch to `LeftNavRail`.

| Tab | Key Surfaces & Responsibilities |
|---|---|
| **LIFE** | 76dp avatar hero header, cash & net worth counters, stat chips, connected year journal timeline, live quests, 3D Age Up Button |
| **ACTIONS** | Drill-down rows: Childhood play (0–15), Leisure & Wellness, Health & Doctors, Crime & Underworld, Financial moves, Relocation |
| **FAMILY** | Family list, dynamic dating candidate sheets (`DatingMatchSheet`), relationship progression, weddings & prenups, pet care |
| **CAREER** | School (Primary/Secondary, exams, roster, clubs), 7 University majors (loans, work-study, honors), Jobs, Corporate Ladders, Workplace agency (`WorkplaceRosterSheet`), Fame/Talent Hub, Businesses, Politics |
| **ASSETS** | Real estate (cash or 30-year amortized mortgage), Luxury vehicles (cars, yachts, speedboats, private jets), Investments, Living tiers, Wills |

### Procedural 2D Avatar Portrait Engine (`ProceduralAvatarPortrait.kt`)
Completely offline Canvas drawing engine (1,180+ lines):
- Renders face shapes, dynamic skin tones, layered hairstyles & colors, eyes, expressive eyebrows, noses, dynamic mouths, facial hair (beards, goatees, mustaches), and accessories (glasses, hats, earrings, necklaces, collars).
- Life stage progression from chubby-cheeked infants to wrinkled elders with graying hair.
- Live expressions reflecting character mental/physical state: Happy, Neutral, Sad/Depressed, Sick/Injured, Stressed/Burned out.
- Under-14 minors are strictly protected from adult facial hair or eyeglasses.

---

## 7. Deep Systems Inventory

### Education & School Life
- Primary (6–12) and Secondary (13–17) with country exam names (KCSE, WAEC, SAT, GCSE, Abitur, etc.).
- Slacker / Normal / Grind study effort modes.
- Dynamic school roster: Classmates, Best Classmate, Bully, Teachers, School Crush.
- Yearly activities limit: 1 academic + 1 social action per year.
- School clubs: Debate, Football/Soccer, Drama, Coding, Music with ranks (*Member* &rarr; *Officer* &rarr; *Captain*), practice intensity, letter jackets, and rivalry matches.
- Club leadership agency: Strategy sessions (`leadClubStrategy`) trading stress for prestige/skills, and junior mentorship (`mentorClubJunior`) building reputation and classmate bonds.
- Misbehavior, detention, rebel achievements, principal appeals, expulsion hearings.

### University & Higher Education
- 7 Majors: CS, Law, Medicine, Business, Communications, Engineering, Nursing.
- Funding: Cash, Student Loans (with real interest and amortization), or Merit Scholarships.
- Campus life: Mixers, professor office hours, all-nighter crams, campus rivalry pranks, work-study jobs, corporate internships.
- Honors (*Cum Laude*, *Magna Cum Laude*, *Summa Cum Laude*) unlocking corporate fast-track offers.

### Workplace Agency & Corporate Ladders
- Corporate ranks: Entry &rarr; Mid &rarr; Senior &rarr; Lead &rarr; Director &rarr; Executive.
- `WorkplaceRosterSheet`: Supervisor (Strictness & Competence) and diverse Coworkers.
- Player actions: Schmooze boss, ask for raise/promotion, collaborate, prank (35% backfire risk), report to HR, request mentorship (`MENTOR_REQUEST`), and pitch innovative projects (`PITCH_PROJECT`).
- Workplace anti-farming: Coworkers track `interactedThisYear` with badge indicators to prevent repeated exploitation.
- Workplace hazards & PIP counter-moves: Performance Improvement Plans (PIPs), credit stealing, burnout, and layoffs. `PipAlertCard` provides interactive recovery via `workPipDeliverable` (+20% performance, guaranteed clearance) and `rebutPipWithHR` (HR appeal).

### Romance, Matchmaking & Weddings
- `DatingMatchSheet`: Browse dating app candidates with occupation, age, Looks bar, Smarts bar, and Crazy/Drama % rating.
- Progression: Casual Dating &rarr; Exclusive &rarr; Engaged &rarr; Married.
- Venues: Courthouse, Local Venue, Luxury Destination.
- Prenup agreements: Partner reaction driven by Crazy and Karma attributes.
- Anti-farming: `interactedTypesThisYear` prevents spamming interactions on the same NPC in a single turn across infants, toddlers, young children, adult children, and spouses.

### Targeted Crime & Incarceration
- Specializations with minor age-gates: Petty crimes (14+ shoplifting/pickpocketing), Burglary & GTA (16+), Armed Heists (18+).
- Crime planning: Target casing (`caseTarget`) grants a +20% success boost on the subsequent crime at the cost of +5 Heat.
- Police Heat system: Dynamic `heatLevel` [0–100] meter with color-coded risk bar; active penalties on criminal operations; countermeasures via `layLow` and `bribeInformant`.
- Trials: Public Defender vs Elite Defense Attorney.
- Prison life: Prison gangs, prison jobs, parole board hearings, escape attempts.

### Assets, Mortgages & Personal Finance Ecosystem
- **Credit Score (300-850):** Dynamic scoring tracking on-time debt payment streaks, default drops (-35), DTI ratio drag, and criminal record penalties. Gates mortgage down-payment tiers (10% to 40%), APR interest rates (5.5% to 12.0%), and approvals (minimum 500 score required).
- **Annual Cash-Flow Breakdown (`computeAnnualCashFlow`):** Itemized ledger computing Gross Income (salary, gigs, rentals, dividends, pension, business, content) minus Deductions (progressive income tax, living costs, mortgage payments, student loans, upkeep, child support, pets) yielding Net Cash Flow.
- **4-State Macroeconomic Cycle:** Simulates `BOOM`, `STEADY`, `INFLATION_SURGE`, and `RECESSION` cycles, impacting property valuations, living costs, investment returns, and corporate layoff frequency. Fully backwards-compatible with legacy saves via `EconomicClimateSerializer`.
- **Junior Trust Piggy Bank:** Safely stashes childhood gift money for minors (0-17), maturing into liquid wallet cash at age 18. Infancy zero-stress protection is strictly preserved.
- **Mortgage Engine & Prepayments:** 30-year fixed amortization with credit-based APR. `prepayMortgagePrincipal` reduces principal balance, lowers future annual installments, and boosts credit score (+15 for prepayment, +25 for full payoff).
- **15 Country-Authentic Financial Instruments:** Tailored financial instruments across all 15 countries with authentic localized naming, risk profiles, and yield rates (Kenya SACCO + Fuliza mobile overdraft living cost buffer, Nigeria FGN T-Bills, South Africa Stokvel, USA 401(k), UK ISA, India PPF, Japan NISA, Philippines Pag-IBIG MP2, Brazil Tesouro Direto, etc.) manageable via `CountryFinanceSheet`.
- **Relocation FX & International Remittances:** Cross-border relocation through `RelocationEngine` converts wealth via `EconomyScaler.convertCurrency` PPP ratios and resets credit baseline to 600. Expatriates can remit cash back home via `RemittanceSheet` to boost family relationships, Happiness, and Karma.
- **Google Play Billing Store (`DynastyVaultStoreSheet`):** Injected `@Singleton BillingManager` with `billing-ktx` 7.1.1, automatic reconnects, and `onResume` pending-purchase recovery. Consumable Dynasty Cash Vouchers scaled dynamically by country economy multipliers, plus permanent unlocks (`unlock_ad_free` disabling AdMob ads, `unlock_creator_pass` granting custom starting stats and perks).
- **Preventative Asset Servicing:** `serviceAsset` restores condition, marks `servicedThisYear`, and shields vehicles/properties from annual wear and critical breakdown failure events.
- **Luxury vehicles:** Cars, speedboats, yachts, private jets with realistic depreciation and upkeep. Dedicated leisure activities (`YACHT_DAY`, `PRIVATE_JET_TRIP`).
- **Tactile UI Dashboard (`AssetsScreen`):** Prominent `FinanceSummaryCard` at the top of the tab with Net Worth, Liquid Cash, Net Cash Flow (+/-), Credit Rating meter, and Economic Climate chip, backed by `CashFlowLedgerSheet`, `DebtLiabilitiesSheet`, `CountryFinanceSheet`, `RemittanceSheet`, and `DynastyVaultStoreSheet`.

### Mortality & Dynasty Legacy
- Natural and accidental mortality checks.
- BitLife-style silk-sash tombstone ribbons (*Loaded*, *Academic*, *Saint*, *Scandalous*, *Fame Icon*, *Cunning*, *Family Anchor*, *Workaholic*, *Mediocre*).
- Continue dynasty as an adult child heir, inheriting wealth per the character's will.

---

## 8. Commit Timeline (Recent Highlights)

| Commit | Summary |
|---|---|
| `0d4d2f0` | Overhaul avatar vector compositor into procedural illustrated 2D portrait engine (`ProceduralAvatarPortrait.kt`) |
| `cb19c03` | Overhaul visual HUD: LeftNavRail, InsetCardContainer, connected Year Timeline, and 3D AgeUpButton |
| `f16e794` | Redesign Character Creation to BitLife-style single-screen New Life flow |
| `c4596bf` | Fix early game bugs: age-gates, character creation polish, infant balance, relationship anti-farming |
| `b75fa73` | BitLife overhaul: DrillDownRow sheets, matchmaking, workplace roster, crime specializations, tombstone ribbons, 44.1kHz audio & haptics |
| `06a54e4` | Overhaul UI design system: Material 3 polish, refined cards, modern hero header, sleek components |
| `e133e66` | Add Play Games Services, localization tooling, snapshot UI updates, mortgages & luxury vehicles |
| `c316e89` | Prefetch public flavor APIs into Room so gameplay can use real streets, art, quotes, and news offline |
| `8aeb4d0` | Expand country job and name catalogs and fetch optional random names from Random User |
| `56091a4` | School life rewrite: classmates, activities, events, Career UI |
| `feature` | Weekly Challenges overhaul: multi-step objective checklist, countdown timer, country-scaled rewards, `WeeklyChallengeCard`, `WeeklyChallengeSheet`, DataStore claim persistence |
| `feature` | Player Agency Expansion: Workplace anti-farming/mentorship/pitches, PIP recovery & HR rebuttals, Club strategy/mentoring, Crime heat/casing & age gates, Mortgage prepayment & preventative servicing |
| `feature` | Multi-Year Narrative Story Arcs & 15-Country Events: `narrative_arcs_events.json`, `requires_event_<id>` / `conflicts_with_event_<id>` gating, 6 multi-step arcs, and authentic universal life stage events |
| `feature` | Compose UI/UX Ergonomics & Adaptive Navigation Pass: Adaptive `BoxWithConstraints` (bottom bar for <600dp compact phones vs `LeftNavRail` for >=600dp wide layouts), memoized `ProceduralAvatarPortrait`, bounded `EventDialogStyled` & `HeirSelectionScreen`, flexible font-scaling on `DrillDownRow` & `AgeUpButton`, and insets polish |
| `hardening` | Persistence, Engine Edge-Cases, AdMob/Play Games & Release Configuration Audit: Kotlinx Serialization null/default coercion (`coerceInputValues = true`), net worth mortgage liability deduction, extreme lifecycle age-gates (0, 13, 14, 16, 18, 60, 100+), zero/multi-heir and estate insolvency guards, Play Games v2 init, and 507 unit tests |
| `feature` | 15-Country Financial Instruments, Relocation FX & Play Billing Store: Localized instruments & SACCO/overdraft, relocation PPP conversions & remittances, Play Billing v7 Dynasty Vault store (`DynastyVaultStoreSheet`, `CountryFinanceSheet`, `RemittanceSheet`), and 586 unit tests |

---

## 9. Known Backlog & Suggested Next Steps

1. **Additional Narrative Packs & Story Arcs:** Expand multi-year storyline event chains across under-represented countries and specific career tracks (e.g. space exploration, politics, pro athletics).
2. **Production Release Prep:** Replace AdMob test IDs in `AdUnitConfig` with production IDs and finalize Google Play Console app assets.
3. **Advanced Analytics & Sound Tuning:** Fine-tune audio latency on lower-end Android Go devices and verify custom haptic cues across Android 12–15.
