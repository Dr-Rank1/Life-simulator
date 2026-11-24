# Maisha (Life Simulator) — Project Overview & Gemini Context

> **Last Updated:** 2026-11-24  
> **Package:** `com.maisha.game`  
> **Repository:** `https://github.com/Dr-Rank1/Life-simulator` (`origin/main`)  
> **Target Platform:** Android (minSdk 26, targetSdk / compileSdk 35)  
> **Primary Audience for this Document:** Gemini AI assistants, developers, and maintainers.

---

## 1. What Maisha Is

**Maisha** (Swahili for *life*) is an offline-first, single-player life simulation mobile game for Android in the BitLife / Age Sim genre. 

Players create a custom character and guide them year-by-year from birth to death and across generations:
1. **Age Up:** Each year starts with an "Age Up" tick where time advances, life events trigger, and core engine calculations run.
2. **Yearly Choices & Events:** The character faces rich contextual narrative events with meaningful trade-offs (affecting happiness, health, smarts, looks, karma, money, and relationships).
3. **Player Agency Between Age-Ups:** Rather than simply tapping "Age Up", players have extensive agency across five core tabs:
   - **Life:** Character status header, cash/net worth, connected year journal timeline, live quests, tactile 3D Age Up primary button.
   - **Actions:** Activities categorized into childhood play, leisure, wellness, crime & underworld, lifestyle, finances, and international relocation.
   - **Family & Relationships:** Parents, siblings, romantic partners, spouses, children, friends, enemies, and pets with interactive candidate matchmaking (`DatingMatchSheet`).
   - **Career & School:** Primary school, secondary school, school clubs (ranks, rivalry matches, letter jackets), 7 university majors (loans, work-study, honors), student part-time jobs, side hustles, corporate career ladders with workplace agency (`WorkplaceRosterSheet`), fame/talent hubs, military, businesses, and politics.
   - **Assets:** Real estate (purchased cash or 30-year amortized mortgage), luxury vehicles (cars, speedboats, yachts, private jets), investments, living standards, and wills/testaments.
4. **Mortality & Dynasty Legacy:** Characters die naturally or through misfortune. They receive BitLife-style silk-sash tombstone ribbons (*Loaded*, *Academic*, *Saint*, *Scandalous*, *Fame Icon*, *Cunning*, *Family Anchor*, *Workaholic*, *Mediocre*). Players can inherit accumulated wealth and continue the family dynasty as one of their surviving children.

### Core Design Philosophy
- **True Global Flavor:** The game natively supports **15 countries** (Kenya, Nigeria, South Africa, Egypt, USA, Canada, UK, France, Germany, India, Japan, Philippines, Indonesia, Brazil, Mexico). It is not a US template with flags swapped: countries have custom currencies, real name catalogs, localized exam systems (e.g., KCSE, WAEC, SAT, GCSE, Abitur, Baccalauréat), realistic transport modes, mobile money apps (M-Pesa, OPay, Venmo, Pix, UPI), holidays, and country-tailored economies.
- **Offline-First:** All core narrative packs and game engines run 100% locally on the device without requiring an internet connection.
- **Player Agency Over Randomness:** The game prioritizes interactive systems, actions, and NPC relationships over pure passive random event generation.

---

## 2. Architecture & Tech Stack

```
┌──────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                     │
│  LifeScreen | ActionsScreen | FamilyScreen | Career...   │
│  LeftNavRail | InsetCardContainer | 3D AgeUpButton       │
│  ProceduralAvatarPortrait (Offline Canvas Engine)        │
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
│  • EventRepository (13+ JSON event packs in assets)      │
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
| **Monetization** | Google Mobile Ads (AdMob) & Play Billing v7 | Interstitials, banners, rewarded revive, country-scaled cash vouchers, and permanent unlocks (`unlock_ad_free`, `unlock_creator_pass`). |
| **Unit Testing** | JUnit 4, Kotlin Test | High-coverage domain tests for all engines (`app/src/test/`). |

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
   - Workplace agency (`WorkplaceRosterSheet`): Supervisor (strictness & competence) and Coworkers; schmoozing, raises, promotions, collaborating, pranks (35% backfire risk), HR reporting.
   - Office hazards: Performance Improvement Plans (PIP), credit-stealing, burnout, and networking.
   - Student part-time jobs (energy and study balance).
   - Youth and adult side hustles.
   - Fame and talent pathways: Pro sports / entertainment tracks, training, street gigs, exhibitions, paparazzi, and endorsements.
4. **`FinanceEngine`**:
   - Dynamic credit score (300-850) gating mortgage approvals, down-payment tiers, and APR rates.
   - Itemized annual cash-flow breakdown (`computeAnnualCashFlow`) computing gross income minus deductions.
   - 4-state macroeconomic cycle (`BOOM`, `STEADY`, `INFLATION_SURGE`, `RECESSION`).
   - 15 Country-authentic financial instruments (SACCO, T-Bills, 401(k), ISA, PPF, NISA, Pag-IBIG MP2, etc.) and mobile overdraft buffers.
   - Relocation PPP FX currency conversions and international remittances (`remitMoneyHome`).
   - 30-year amortized mortgages with principal prepayments.
   - Luxury assets (cars, speedboats, yachts, private jets) with depreciation, upkeep, and preventative servicing.
   - Junior trust piggy bank for minors (0-17) unlocking at age 18.
5. **`RelationshipEngine`**:
   - Family (parents, siblings, children), dating, proposals, weddings, prenups, affairs, and divorces.
   - **Interactive Matchmaking:** `DatingMatchSheet` candidate profiles (age, occupation, Looks, Smarts, Crazy/Drama %).
   - Relationship progression: Casual &rarr; Exclusive &rarr; Engaged &rarr; Married; prenups driven by partner Crazy/Karma.
   - Anti-farming protection (`interactedTypesThisYear`).
   - Pet adoption, veterinary care, feeding, training, and mortality.
6. **`SocialEcosystemEngine` & `LegacyEngine`**:
   - Recurring rivals, mentors, benefactors, family feuds, and alliances that persist across decades and pass to designated heirs.
   - Neighborhood reputation and multi-generational family memory shaping opportunities and NPC reactions.
7. **`RoutineDilemmaEngine`**:
   - Fast-resolving snap decisions during everyday routines (commute via `{transportMode}`, study crams, work crunch, weekend splurges).
8. **`HealthEngine`**:
   - Health deterioration, acute diseases, chronic illnesses, and medical treatment tiers (Free, Private, Alternative).
9. **`CrimeEngine`**:
   - Targeted crime specializations: Shoplifting, Pickpocketing, Grand Theft Auto (GTA), Burglary, Armed Heists.
   - Lawyer representation (Public Defender vs Elite Attorney), trials, prison yard life (gangs, jobs, parole), and escape attempts.
10. **`LeisureEngine`**:
    - Hobbies, gym, meditation, vacations, yacht days, and private jet trips.
11. **`BusinessEngine` & `PoliticsEngine`**:
    - Founding, funding, and managing startups; political campaigns, municipal/national office, and tax policy.
12. **`RelocationEngine`**:
    - Emigration, work visas, permanent residency, and expat restrictions across 15 countries.
13. **`AchievementEngine`, `MilestoneEngine`, `YearQuestEngine`, `WeeklyChallengeEngine`, `LifeArchetypeEngine`**:
    - Player progression, dynamic yearly goals, live era/chapter titles, evocative epitaphs, silk-sash tombstone ribbons, and heir succession.

---

## 4. UI System & Design Language

The UI features a modern **Material 3 simulator aesthetic**:
- **Palette (`Color.kt`):**
  - Canvas: Warm crisp neutral `CanvasBg = #F8F9FA` / `SurfaceMuted = #EFF1F5`.
  - Cards: Pure elevated `PaperWhite = #FFFFFF`.
  - Typography: High-contrast slate hierarchy (`#1E293B`, `#64748B`, `#94A3B8`).
  - Outlines: Elegant 1dp hairlines (`#E2E8F0` / `#CBD5E1`).
  - Primary Accent: Fresh emerald `LifeGreen = #22C55E`, `LifeGreenDark = #16A34A`, `LifeGreenSoft = #DCFCE7`.
  - Stat Tokens: Happiness (Amber `#F59E0B`), Health (Emerald `#10B981`), Smarts (Indigo `#6366F1`), Looks (Fuchsia `#D946EF`), Karma (Violet `#8B5CF6`).
- **Components:**
  - **Procedural 2D Avatar (`ProceduralAvatarPortrait.kt`):** 100% offline Canvas-rendered portraits compositing skin, hair, facial features, expressions, and accessories with age and emotional progression.
  - **Hero Header (`LifeScreen.kt`):** 76dp avatar with status ring, country flag + age pill, cash & net worth counters, and a 2x2 grid of micro-progress chips.
  - **Connected Year Timeline (`LifeScreen.kt`):** Chronological event log with vertical milestone tracks and auto-scrolling.
  - **3D Age Up Button (`AgeUpButton.kt`):** Full-width emerald 3D gradient button with spring press physics, layered depth, and tactile haptic feedback.
  - **Adaptive Navigation:** `LeftNavRail` for tablets/foldables/landscape alongside `BottomNav` for phones.
  - **DrillDownRow List Pattern:** Eliminates nested card sprawl by opening dedicated `ModalBottomSheet`s for actions, family, career, and assets.

---

## 5. Recent Architecture & Feature Additions

1. **Procedural Illustrated 2D Portrait Engine (`0d4d2f0`)**:
   - Completely replaces external vector URLs with a local, 100% offline Canvas-rendered portrait compositor (`ProceduralAvatarPortrait.kt`). Supports custom hair textures, colors, skin tones, facial features, facial hair, accessories, and emotional/age progression.
2. **Visual HUD Overhaul (`cb19c03`)**:
   - Added responsive `LeftNavRail` for larger screens and landscape orientation.
   - Implemented `InsetCardContainer` with elevated card styling.
   - Connected Year Timeline with milestone pins and auto-scroll in `LifeScreen.kt`.
   - Tactile `3D AgeUpButton` with spring physics and audio-haptic sync.
3. **BitLife-Style Single-Screen Character Creation (`f16e794`)**:
   - Redesigned `CharacterCreationScreen.kt` to an all-in-one flow: Name input + Randomizer, Gender, Country selection cards with flags, 140-point Stats budget stepper/presets, and instant procedural avatar preview.
4. **Early Game Age-Gating, Infant Balancing & Character Polish (`c4596bf`)**:
   - Infancy (0–2): Exclusively baby interactions (*Cuddle*, *Babble*, *Tantrum*); shielded from debt stress and macroeconomic inflation spam.
   - Strict age locks: Part-time jobs (16+), full-time careers & businesses (18+), independent health insurance (18+), minor asset blocker (`DependentMinorCard`).
   - Anti-exploit protection: `interactedTypesThisYear` stops infinite relationship farming.
   - Pranks carry a 35% backfire risk.
   - Under-14 minors strictly protected from adult facial hair or eyeglasses.
5. **DrillDownRow Architecture, Matchmaking & Workplace Agency (`b75fa73`)**:
   - Transformed all 5 tabs to clean `DrillDownRow` list pattern opening `ModalBottomSheet`s.
   - Interactive dating candidate cards (`DatingMatchSheet.kt`) with occupation, Looks, Smarts, and Crazy/Drama % rating.
   - `WorkplaceRosterSheet.kt`: Supervisor and coworkers with schmoozing, raises, promotions, pranks, and HR reports.
   - Targeted crime specializations (Burglary, GTA, Heists), lawyers, trials, prison yard gangs, and parole petitions.
   - Silk-sash tombstone ribbons (*Loaded*, *Academic*, *Saint*, *Scandalous*, etc.) and multi-generational dynasty succession.
   - 16-bit 44.1kHz synthesized audio and multi-waveform haptic vibration patterns.
6. **Tactile Dashboard Overhauls (Another Life & BitLife Style)**:
   - Career Hero Card with corporate tier stepper, salary/raise projection pills, job opportunity cards with 2-question interview mini-checks, and PIP deliverable recovery.
   - `PersonDetailSheet.kt` with 64dp procedural avatar synced to emotions, drama meters, speech bubble thought quotes, floating delta pills, and 2-column vector action tiles with anti-farming tracking.
7. **Living Social Ecosystem & Generational Memory (`SocialEcosystemEngine.kt`)**:
   - Recurring rivals, mentors, benefactors, family feuds, and alliances that persist across years and pass into heir succession.
8. **Routine Micro-Dilemmas & Snap Decisions (`RoutineDilemmaEngine.kt`)**:
   - Snappy dilemmas during daily routines (commuting via `{transportMode}`, late-night cramming, work crunch, weekend splurges) with crisp stat trade-offs.
9. **Dynamic Chapter Titles & Evocative Epitaphs (`LifeArchetypeEngine.kt`)**:
   - Live era titles reflecting current life stage and achievements; rich obituaries and legacy stamps.
10. **15-Country Finance, Remittances & Google Play Billing v7**:
    - Dynamic credit score (300-850), itemized annual cash-flow breakdown, 4-state macroeconomic cycle, junior trust piggy bank (0-17).
    - 15 Country-authentic financial instruments (SACCO, T-Bills, 401(k), ISA, PPF, NISA, Pag-IBIG MP2, etc.) and mobile overdraft buffer.
    - Relocation PPP FX currency conversion and international remittances (`remitMoneyHome`).
    - Google Play Billing v7 (`BillingManager.kt`): consumable country-scaled cash vouchers and permanent unlocks (`unlock_ad_free`, `unlock_creator_pass`).

---

## 6. Guidelines for AI Assistants & Gemini Pair Programmers

1. **Respect Domain Engine Purity:** Never import Android framework classes (`Context`, `Bitmap`, `View`, `Compose`) inside `app/src/main/java/com/maisha/game/domain/`.
2. **State Flows and ViewModels:** The UI reads state exclusively through `StateFlow` from ViewModels. Never mutate engine models directly from Composables.
3. **Strings and Localization:** Always place user-facing copy in `res/values/strings.xml` with appropriate translation keys. Never hardcode strings in UI composables.
4. **Country Neutrality:** Default logic must remain country-agnostic or draw from country catalogs. Restrict nation-specific logic to `restrictedToCountry` or catalog parameters.
5. **Content Authoring:** When adding narrative events to `assets/data/events/`, always register them in `EventRepository.kt` and add them to `GlobalContentCoverageTest.kt`.
6. **Documentation Location:** All project documentation is centralized in the root `docs/` folder. Keep `README.md`, `GAME_OVERVIEW.md`, `GEMINI.md`, `AI_CONTINUITY_PROMPT.md`, `EVENTS_SYSTEM.md`, and `PRIVACY_POLICY.md` updated and in sync.
7. **Verification Commands:** Always verify code changes with:
   ```bash
   ./gradlew assembleDebug
   ./gradlew testDebugUnitTest
   ```
