# Maisha (Life Simulator) — Project Overview & Gemini Context

> **Last Updated:** 2026-11-02  
> **Package:** `com.maisha.game`  
> **Repository:** `https://github.com/stewiriffin/Life-simulator` (`origin/main`)  
> **Target Platform:** Android (minSdk 26, targetSdk / compileSdk 35)  
> **Primary Audience for this Document:** Gemini AI assistants, developers, and maintainers.

---

## 1. What Maisha Is

**Maisha** (Swahili for *life*) is an offline-first, single-player life simulation mobile game for Android in the BitLife / Age Sim genre. 

Players create a custom character and guide them year-by-year from birth to death and across generations:
1. **Age Up:** Each year starts with an "Age Up" tick where time advances, life events trigger, and core engine calculations run.
2. **Yearly Choices & Events:** The character faces rich contextual narrative events with meaningful trade-offs (affecting happiness, health, smarts, looks, karma, money, and relationships).
3. **Player Agency Between Age-Ups:** Rather than simply tapping "Age Up", players have extensive agency across five core tabs:
   - **Life:** Character status header, cash/net worth, yearly recap, live quests, Age Up primary button.
   - **Actions:** Activities categorized into leisure, crime, lifestyle, health/wellness, childhood play, and financial decisions.
   - **Family & Relationships:** Parents, siblings, romantic partners, spouses, children, friends, enemies, and pets.
   - **Career & School:** Primary school, secondary school, school clubs, university degrees, student jobs, side hustles, corporate career ladders, fame/talent hubs, military, businesses, and politics.
   - **Assets:** Real estate (purchased cash or mortgage), luxury vehicles (cars, speedboats, yachts, private jets), investments, living standards, and wills/testaments.
4. **Mortality & Dynasty Legacy:** Characters die naturally or through misfortune. Players can inherit their accumulated wealth and continue the family dynasty as one of their children.

### Core Design Philosophy
- **True Global Flavor:** The game natively supports **15 countries** (Kenya, Nigeria, South Africa, Egypt, USA, Canada, UK, France, Germany, India, Japan, Philippines, Indonesia, Brazil, Mexico). It is not a US template with flags swapped: countries have custom currencies, real name catalogs, localized exam systems (e.g., KCSE, SAT, GCSE, Abitur, Baccalauréat), realistic holiday schedules, and country-tailored economy scaling.
- **Offline-First:** All core narrative packs and game engines run 100% locally on the device without requiring an internet connection.
- **Player Agency Over Randomness:** The game prioritizes interactive systems, actions, and NPC relationships over pure passive random event generation.

---

## 2. Architecture & Tech Stack

```
┌──────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                     │
│  LifeScreen | ActionsScreen | FamilyScreen | Career...   │
└────────────────────────────┬─────────────────────────────┘
                             │ StateFlow
┌────────────────────────────▼─────────────────────────────┐
│                 Hilt ViewModels (MVVM)                   │
│           LifeViewModel, CharacterCreationViewModel      │
└────────────────────────────┬─────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────┐
│           GameEngine (Central Orchestrator)              │
├──────────────────────────────────────────────────────────┤
│ Pure Domain Engines:                                     │
│  • EducationEngine       • CareerEngine                  │
│  • FinanceEngine         • RelationshipEngine            │
│  • HealthEngine          • CrimeEngine                   │
│  • LeisureEngine         • PoliticsEngine                │
│  • BusinessEngine        • RelocationEngine              │
│  • MilestoneEngine       • LegacyEngine ...              │
└────────────────────────────┬─────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────┐
│             Data & Persistence Layer                     │
│  • Room Database (Multi-slot saves & Room entities)      │
│  • DataStore Preferences (Audio, haptics, settings)      │
│  • EventRepository (JSON event packs in assets)          │
│  • PlayGamesService (Cloud saves & Leaderboards)         │
└──────────────────────────────────────────────────────────┘
```

### Languages & Frameworks
| Layer | Technologies | Details |
|---|---|---|
| **Language** | Kotlin 2.0+ | 100% Kotlin codebase, targeting Java 17 bytecode. |
| **UI Framework** | Jetpack Compose + Material 3 | Modern declarative UI, custom design tokens, spring animations, custom Canvas rendering. |
| **Architecture** | MVVM + Clean Architecture | Strict separation between pure domain rules and Android UI. |
| **Dependency Injection** | Dagger Hilt | `@HiltAndroidApp`, `@HiltViewModel`, modular DI structure. |
| **Persistence** | Room + DataStore | Room for multi-slot character saves and offline cache; DataStore for preferences. |
| **Serialization** | Kotlinx Serialization | JSON parser for narrative packs and deep state serialization. |
| **Concurrency** | Kotlin Coroutines & Flow | Asynchronous event processing and reactive `StateFlow` state streams. |
| **Background Tasks** | AndroidX WorkManager | Contextual daily notifications and retention nudges. |
| **Cloud & Social** | Google Play Games Services v2 | Cloud save snapshots (`SnapshotsClient`) and global leaderboards. |
| **Monetization** | Google Mobile Ads (AdMob) | Interstitial on age-ups, banner on slot picker, rewarded Second Wind revive. |
| **Unit Testing** | JUnit 4, Kotlin Test, Mockito | High-coverage domain tests for all engines (`app/src/test/`). |

---

## 3. Domain Engine Inventory (The Brain)

All business rules live in `app/src/main/java/com/maisha/game/domain/`.
> **Critical Rule:** Domain engines must remain **pure Kotlin**. They do not touch Android `Context`, Compose, or Room. They take immutable models and return updated copies.

1. **`GameEngine`**: The central coordinator. Ticks the yearly age loop, orchestrates all sub-engines in sequence, and provides facades for ViewModels.
2. **`EducationEngine`**:
   - Manages school stages: Primary, Secondary, University.
   - School life roster: Classmates, Best Classmate, Teachers, Bully, and Crush.
   - Yearly academic & social activity limits (study group, library study, hang out, confront bully, skip class, school dance).
   - School clubs (Debate, Football, Drama, Coding, Music) with ranks, practice intensity, letter jackets, rival matches, and scholarships.
   - Gamified exams with preparation, stress, and cheating risks.
   - Detention, misbehavior, principal appeals, and expulsion hearings.
   - University system: 7 majors (CS, Law, Medicine, Business, Communications, Engineering, Nursing), funding methods (Cash, Loan, Scholarship), campus jobs, internships, honors, and post-grad career track offers.
3. **`CareerEngine`**:
   - Country job pools, qualifications, hiring tests, promotions, and firings.
   - Corporate ladder progression (Entry &rarr; Mid &rarr; Senior &rarr; Lead &rarr; Director &rarr; Executive).
   - Office politics: Performance Improvement Plans (PIP), credit-stealing, burnout, and networking.
   - Student part-time jobs (energy and study balance).
   - Youth and adult side hustles.
   - Fame and talent pathways: Pro sports / entertainment tracks, training, street gigs, exhibitions, paparazzi, and endorsements.
4. **`FinanceEngine`**:
   - Cash flow, tax calculations, living expenses, investments, and wills.
   - **Mortgages:** 20–50% down payment, 30-year amortization, annual payments, and equity resolution on sale.
   - **Luxury Assets:** Cars, speedboats, yachts, and private jets with accurate depreciation and monthly upkeep.
   - Student loan interest accrual and repayment.
5. **`RelationshipEngine`**:
   - Family (parents, siblings, children), dating, proposals, weddings, prenups, affairs, and divorces.
   - Pet adoption, veterinary care, feeding, training, and mortality.
   - Dynamic relationship tiers and annual bond decay.
6. **`HealthEngine`**:
   - Health deterioration, acute diseases, chronic illnesses, and medical treatment tiers.
7. **`CrimeEngine`**:
   - Petty crime, grand theft, drug running, arrests, lawyer representation, trials, prison sentences, and escape attempts.
8. **`LeisureEngine`**:
   - Hobbies, gym, meditation, vacations, yacht days, and private jet trips.
9. **`BusinessEngine` & `PoliticsEngine`**:
   - Founding, funding, and managing startups; political campaigns, municipal/national office, and tax policy.
10. **`RelocationEngine`**:
    - Emigration, work visas, permanent residency, and expat restrictions across 15 countries.
11. **`AchievementEngine`, `MilestoneEngine`, `YearQuestEngine`, `WeeklyChallengeEngine`, `LegacyEngine`**:
    - Player progression, dynamic yearly goals, heir inheritance, and family dynasty trees.

---

## 4. UI System & Design Language

The UI has undergone a comprehensive **Material 3 modernization**:
- **Palette (`Color.kt`):**
  - Canvas: Warm crisp neutral `CanvasBg = #F8F9FA` / `SurfaceMuted = #EFF1F5`.
  - Cards: Pure elevated `PaperWhite = #FFFFFF`.
  - Typography: High-contrast slate hierarchy (`#1E293B`, `#64748B`, `#94A3B8`).
  - Outlines: Elegant 1dp hairlines (`#E2E8F0` / `#CBD5E1`) replacing dated heavy comic borders.
  - Primary Accent: Fresh emerald `LifeGreen = #22C55E`, `LifeGreenDark = #16A34A`, `LifeGreenSoft = #DCFCE7`.
  - Stat Tokens: Happiness (Amber `#F59E0B`), Health (Emerald `#10B981`), Smarts (Indigo `#6366F1`), Looks (Fuchsia `#D946EF`), Karma (Violet `#8B5CF6`).
- **Components:**
  - **Hero Header (`LifeScreen.kt`):** 76dp avatar with status ring, country flag + age pill, cash & net worth counters, and a 2x2 grid of micro-progress chips (`ModernStatChip`).
  - **Age Up Button (`AgeUpButton.kt`):** Full-width emerald gradient button with smooth spring press scale animation and tactile shadow.
  - **Bottom Navigation (`BottomNav.kt`):** Floating modern pill selection indicator with crisp vector icons and badge counters.
  - **Action & Person Cards (`ActionCard.kt`, `PersonCard.kt`):** Elevated white cards with category-tinted circular icon badges and clear descriptions.

---

## 5. Recent Progress (What Has Shipped)

1. **Complete Material 3 UI Overhaul (`06a54e4`)**:
   - Fully transformed every screen (Life, Career, Actions, Family, Assets, Character Creation, Dialogs) from harsh comic borders to an elegant modern simulator aesthetic.
2. **Google Play Games Services v2 Integration (`e133e66`)**:
   - Added `PlayGamesService.kt` for cloud save snapshots and real-time leaderboard score submissions.
   - Configured `LeaderboardIds.kt` for Net Worth, Longest Life, Generations, and Happiness.
3. **Mortgage & Luxury Vehicles Engine (`e133e66`)**:
   - Added full mortgage financing to `FinanceEngine.kt` and `Asset.kt`.
   - Added Speedboat, Yacht, and Private Jet with custom leisure activities (`YACHT_DAY`, `PRIVATE_JET_TRIP`).
4. **Multilingual Backfill Tooling (`scripts/backfill_translations.py`)**:
   - Automated script using the Gemini API to scan `values/strings.xml` and generate consistent translations across Spanish, French, Hindi, Swahili, and Portuguese while preserving format specifiers.
5. **Deep School & University Experience (`58a2ca7`, `84ced90`)**:
   - Roster of classmates and teachers with individual relationships.
   - Comprehensive university system: majors, student loans, work-study, internships, and post-grad tracks.
6. **Career Ladders & Office Politics (`145411b`, `f809b30`)**:
   - Multi-step corporate promotion ladders, burnout, PIP, and alternative fame/talent career paths.
7. **BitLife & Another Life Simulator Architecture Overhaul**:
   - Complete `DrillDownRow` list pattern and `ModalBottomSheet` drill-down architecture across all 5 main tabs (Actions, Career, Assets, Family, Life), eliminating giant stacked card walls for 360dp mobile screens.
   - Main `LifeScreen` journal auto-scroll (`animateScrollToItem(0)`) with gold milestone pills and chronological intra-year log grouping.
   - Childhood & teen action density (Ages 0–15): toys, tantrums, allowances, hobbies, chores, sneaking out, mall hangouts, gaming marathons.
   - University social life & academic agency: campus mixers & Greek life, professor office hours, all-nighter exam crams, campus rivalry pranks.
   - High-fidelity 16-bit 44.1kHz audio synthesis replacing placeholder silent `.wav` files with tactile sound effects, plus multi-waveform haptic vibration patterns.
8. **Deep Gameplay Systems, Matchmaking, Workplace Agency & Endgame Polish**:
   - **Interactive Matchmaking & Dating App Sheet (`DatingMatchSheet.kt`)**: Replaced blind RNG dating with realistic candidates featuring age, occupation badge, Looks, Smarts, and Crazy/Drama % stat bars with Pass and Ask Out actions.
   - **Relationship Progression & Wedding Tiers (`RelationshipEngine.kt`)**: Casual -> Exclusive -> Engaged -> Married progression, Courthouse/Local/Destination wedding venues, and prenuptial agreement reactions driven by partner Crazy/Karma.
   - **Workplace Roster & Office Agency (`WorkplaceRosterSheet.kt`, `CareerEngine.kt`)**: Active jobs generate a supervisor and coworkers with strictness/competence stats, enabling actions like Schmooze, Ask for Raise/Promotion, Collaborate, Prank, and HR reporting.
   - **Crime Specialization & Prison Life (`CrimeEngine.kt`)**: Target selection across Burglary, GTA, and Armed Heist tiers with criminal notoriety scaling; prison yard gangs, parole petitions, and escape attempts.
   - **Tombstone Ribbons & Heir Picker (`LifeArchetypeEngine.kt`, `LifeSummaryScreen.kt`, `HeirSelectionScreen.kt`)**: BitLife-style silk-sash tombstone ribbons (Loaded, Academic, Cunning, Saint, Scandalous, Fame Icon, Family Anchor, Workaholic, Mediocre) and polished heir succession cards.
9. **Early Game Age-Gating, Infant Balancing & Character Creation Polish**:
   - **Character Creation UI Polish**: Accurate stat note, horizontal country pill cards with empty state search, `FlowRow` preventing chip clipping/squishing, and suppression of adult beards/glasses for characters under 14.
   - **Strict Age-Gating Across All Tabs**: Infants (0–2) only access baby interactions (Cuddle/Bond, Babble/Smile, Tantrum); school stage features clean `EarlyChildhoodCard` for `< 6`; part-time jobs lock until 16, full-time career and businesses lock until 18; minors `< 16` see `DependentMinorCard` on Assets; independent health insurance gated to `18+`.
   - **Anti-Exploit & Balance Fixes**: Track `interactedTypesThisYear` per NPC to prevent infinite relationship farming; add 35% prank backfire risk; eliminate newborn debt stress drain and macroeconomic log spam for assetless minors.
   - **Visual Consistency**: Distinct Material 3 `Icons.Rounded` icons throughout all tabs replacing placeholder graphics.

---

## 6. What Is Needed Next (Active Roadmap)

When picking up development or proposing next steps, prioritize these areas:

1. **Dedicated UI for Weekly Challenges**:
   - `WeeklyChallengeEngine` is fully functional in domain code; elevate with a dedicated, prominent weekly challenge card/sheet with countdown timers and exclusive dynasty badges.
2. **Additional Narrative Packs & Story Arcs**:
   - Expand multi-year storyline event packs across under-represented countries and specific career branches (e.g. politics, space exploration, professional athletics).
3. **Production Release Polish**:
   - Swap test AdMob IDs in `AdUnitConfig` with production IDs before launch.
   - Configure official Google Play Games App ID in Google Play Console.

---

## 7. Guidelines for AI Assistants & Gemini Pair Programmers

1. **Respect Domain Engine Purity:** Never import Android framework classes (`Context`, `Bitmap`, `View`, `Compose`) inside `app/src/main/java/com/maisha/game/domain/`.
2. **State Flows and ViewModels:** The UI reads state exclusively through `StateFlow` from ViewModels. Never mutate engine models directly from Composables.
3. **Strings and Localization:** Always place user-facing copy in `res/values/strings.xml` with appropriate translation keys. Never hardcode strings in UI composables.
4. **Country Neutrality:** Default logic must remain country-agnostic or draw from country catalogs. Restrict Kenya-specific or nation-specific logic to `restrictedToCountry` or catalog parameters.
5. **Content Authoring:** When adding narrative events to `assets/data/events/`, always register them in `EventRepository.kt` and add them to `GlobalContentCoverageTest.kt`.
6. **Verification Commands:** Always verify code changes with:
   ```bash
   ./gradlew assembleDebug
   ./gradlew testDebugUnitTest
   ```
