# Life Event & Narrative Engine Documentation (`docs/EVENTS_SYSTEM.md`)

> **Location:** Consolidated documentation for narrative events stored in `app/src/main/assets/data/events/`.  
> **Target:** Game designers, narrative writers, developers, and AI assistants.

---

## 1. Overview & Architecture

Narrative **life events** are the storytelling backbone of Maisha. Each turn (calendar year), after domain engines update character stats, the `EventRepository` queries the merged pool of authored events. Eligible events are filtered by age, career status, education level, relationship gates, and country restrictions, then interpolated with authentic local tokens (transport, exams, banking apps) before being presented in `EventDialogStyled` modals.

System-generated mechanical events (such as national exam results, corporate promotions, firings, corporate downsizing, and relocation job offers) are generated directly in domain engines (`EducationEngine`, `CareerEngine`, `RelocationEngine`). Narrative events live in 18 specialized JSON asset packs.

```mermaid
flowchart TD
    JSON[18 JSON Event Packs in assets] --> Repo[EventRepository.loadAllFromAssets]
    Repo --> Filter{getEligibleEvents}
    Filter -->|Check Age Range| G1[Age Gating]
    Filter -->|Check Tags: requires_job, education, etc.| G2[State & Career Gating]
    Filter -->|Check Country: restrictedToCountry| G3[Country Gating]
    G1 & G2 & G3 --> EligiblePool[Filtered Eligible Event Pool]
    EligiblePool --> Picker[pickRandomEvent Weighted Roll]
    Picker --> Interpolator[FlavorInterpolator]
    Interpolator -->|Inject {transportMode}, {moneyApp}, etc.| LocalizedEvent[Localized LifeEvent]
    LocalizedEvent --> UI[EventDialogStyled Modal]
```

---

## 2. Event Pack Inventory (18 JSON Files)

All narrative event packs reside in `app/src/main/assets/data/events/`:

| File | Scope / Theme | Typical Age Range | Primary Gates & Tags |
|---|---|:---:|---|
| `starter_events.json` | Newborn, infant, and toddler moments | 0–5 | Baby milestones, parenting interactions |
| `education_events.json` | School life, exam preparations, homework dilemmas | 6–22 | `education`, `study_effort` |
| `school_life_events.json` | Classmate secrets, crushes, bullies, clubs, rivalries | 6–18 | `education`, classmates roster interactions |
| `career_events.json` | Workplace dilemmas, promotions, office politics | 18–70 | `career`, `requires_job`, `work_effort` |
| `wave4_workplace_events.json` | Advanced corporate politics, PIPs, boss interactions | 18–65 | `requires_job`, corporate tracks |
| `wave4_side_hustle_events.json`| Freelancing, streaming, craft businesses | 14–60 | Side hustle management |
| `wave4_hobbies_events.json` | Creative pursuits, sports, gaming, outdoor activities | 8–80 | Leisure & hobbies |
| `relationship_events.json` | Family dynamics, dating dilemmas, marriage choices | 13–80 | `relationship`, `requires_spouse`, `requires_child` |
| `wave4_love_sex_events.json` | Intimate romance, relationship tests, proposals | 16–70 | `requires_spouse`, `requires_partner` |
| `wave4_children_events.json` | Parenting dilemmas, child milestones, family drama | 18–80 | `requires_child` |
| `finance_events.json` | Money windfalls, scams, asset purchases, market dips | 18–85 | `finance`, wealth thresholds |
| `crime_events.json` | Street crime encounters, temptation, illegal offers | 14–65 | `crime`, law enforcement encounters |
| `wave4_crime_events.json` | Underworld syndicates, heist planning, prison life | 16–70 | `crime`, `incarcerated` |
| `midlife_events.json` | Midlife crises, career shifts, health warnings | 35–65 | Mature adulthood dilemmas |
| `general_events.json` | Broad cross-cutting life moments (largest pool) | 6–90 | Universal human experiences |
| `holiday_events.json` | Country holiday celebrations and family traditions | 4–90 | `holiday`, holiday cooldowns |
| `wave3_events.json` | Specialized medical, legal, and lifestyle dilemmas | 18–80 | Wave 3 system integrations |
| `narrative_arcs_events.json` | Cohesive multi-year narrative story arcs and country-authentic life stage milestones | 7–95 | `one_time`, `requires_event_<id>`, `education`, `requires_job`, `holiday` |

---

## 3. JSON Schema Specification

### Root Structure
```json
{
  "events": [
    /* Array of LifeEvent objects */
  ]
}
```

### `LifeEvent` Object
| Field | Type | Required | Default | Description |
|---|---|:---:|:---:|---|
| `id` | `String` | Yes | — | Globally unique identifier across all event files. |
| `minAge` | `Int` | Yes | — | Minimum character age (inclusive) for eligibility. |
| `maxAge` | `Int` | Yes | — | Maximum character age (inclusive) for eligibility. |
| `text` | `String` | Yes | — | Narrative prompt shown to player. Supports flavor tokens. |
| `choices` | `EventChoice[]`| Yes | — | Array of at least one selectable choice. |
| `weight` | `Int` | No | `1` | Relative weighting in `pickRandomEvent`. Higher = more frequent. |
| `tags` | `String[]` | No | `[]` | Gating and classification tags (see Tags section). |
| `restrictedToCountry` | `String` | No | `null` | ISO-3166-1 alpha-2 code (e.g. `"KE"`). Only triggers in that nation. |

### `EventChoice` Object
| Field | Type | Default | Description |
|---|---|:---:|---|
| `label` | `String` | — | Text displayed on the choice button. |
| `resultText` | `String` | — | Narrative recap prepended to the event journal log on selection. |
| `statEffects` | `Map<String, Int>`| `{}` | Clamped deltas for: `health`, `happiness`, `smarts`, `looks`, `money`. |
| `gpaEffect` | `Float` | `0.0` | Delta applied to student GPA (0.0 to 4.0 scale). |
| `familyRelationshipEffect` | `Int` | `0` | Delta to parent relationship levels. |
| `siblingRelationshipEffect` | `Int` | `0` | Delta to sibling relationship levels. |
| `spouseRelationshipEffect` | `Int` | `0` | Delta to spouse/partner relationship level. |
| `performanceEffect` | `Int` | `0` | Delta to job performance rating. |
| `conditionEffect` | `Int` | `0` | Delta to asset condition percentage. |
| `targetAssetType` | `String` | `null` | Asset type for condition effect (e.g. `"HOUSE"`, `"CAR"`). |
| `universityCourse` | `String` | `null` | Major name to trigger university enrollment flow. |
| `careerTrackStart` | `String` | `null` | Post-grad career track offer (e.g. `"SOFTWARE"`, `"MEDICAL"`). |
| `triggersHaveChild` | `Boolean` | `false` | Triggers the pregnancy / birth sequence. |
| `triggersCrime` | `String` | `null` | Initiates criminal action: `"PICKPOCKET"`, `"SHOPLIFT"`, `"FRAUD"`. |
| `triggersIllnessRoll` | `Boolean` | `false` | Forces an immediate illness roll. |
| `doctorCareTier` | `String` | `null` | Initiates healthcare flow: `"free"`, `"private"`, `"alternative"`. |
| `relocateToCountry` | `String` | `null` | Triggers international relocation to target country code. |

---

## 4. Gating Tags Reference

Tags are evaluated by `EventRepository` against the active `Character` state:

| Tag | Gate Evaluation Condition |
|---|---|
| `one_time` | Fires at most once per life. Tracked in `character.triggeredEventIds`. |
| `requires_event_<id>` | Prerequisite event gating. Only eligible if event `<id>` was triggered in this life (`id in triggeredEventIds`). Used to construct multi-year story arcs. |
| `conflicts_with_event_<id>` | Mutually exclusive narrative branching. Ineligible if event `<id>` was triggered in this life. |
| `education` | Requires active enrollment in Primary, Secondary, or University (`passesEducationGate`). |
| `requires_job` | Requires current employment (`passesCareerGate`). |
| `requires_spouse` | Character must have an active spouse. |
| `requires_married` | Character must be legally married. |
| `requires_child` | Character must have at least one living child. |
| `requires_single` | Character must not have an active romantic partner. |
| `finance` | Requires character to own assets or possess $\ge 50,000$ cash. |
| `holiday` | Country holiday event; subject to cooldown via `lastHolidayAge`. |
| `study_effort` | Choice labels parse for study intensity (slacker, normal, grind). |
| `work_effort` | Choice labels parse for career effort (coast, normal, grind). |
| `exam_system` | Excluded from the random narrative pool (handled by `EducationEngine`). |
| `career_system` | Excluded from the random narrative pool (handled by `CareerEngine`). |
| `relocation_system`| Excluded from the random narrative pool (handled by `RelocationEngine`). |

---

## 5. Country Flavor Placeholders

Authors should avoid hardcoding nation-specific terms in universal events. Instead, use dynamic curly-brace tokens resolved at runtime by `FlavorInterpolator` via `CountryCatalog.flavorFor(countryCode)`:

| Placeholder | Token Source Field | Kenya (`KE`) | Nigeria (`NG`) | USA (`US`) | Philippines (`PH`) |
|---|---|---|---|---|---|
| `{transportMode}` | `commonTransportMode` | matatu | danfo | subway | jeepney |
| `{primaryExam}` | `primaryExamName` | KCPE | Common Entrance | State Standardized Test | DepEd NAT |
| `{secondaryExam}` | `secondaryExamName` | KCSE | WAEC | SAT | DepEd NAT |
| `{moneyApp}` | `popularMoneyAppOrBank` | M-Pesa | OPay | Venmo | GCash |
| `{greeting}` | `greetingPhrase` | Jambo | How far | Hey there | Kumusta |
| `{holidayName}` | `HolidayFlavor.name` | Jamhuri Day | Independence Day | Thanksgiving | Rizal Day |

### Example Universal Event
```json
{
  "id": "teen_commute_hustle",
  "minAge": 14,
  "maxAge": 17,
  "weight": 3,
  "text": "On your morning {transportMode} ride, classmates are bragging about quick cash earned via {moneyApp}.",
  "tags": ["education"],
  "choices": [
    {
      "label": "Ask how it works",
      "statEffects": { "money": 1500, "smarts": 1 },
      "resultText": "You downloaded the app and picked up some basic hustle tips."
    },
    {
      "label": "Review your {secondaryExam} notes instead",
      "statEffects": { "smarts": 2 },
      "resultText": "You ignored the chatter and studied hard for the {secondaryExam}."
    }
  ]
}
```

---

## 6. Authoring & Verification Checklist

1. **Pick the Right Pack:** Place the event in the appropriate JSON file based on its theme.
2. **Globally Unique ID:** Ensure the `id` is unique across all 17 event packs.
3. **Age Bounds:** Match `minAge` and `maxAge` to the intended life stage.
4. **Appropriate Gating:** Add tags like `education` or `requires_job` to prevent nonsensical contexts (e.g. an unemployed infant getting a workplace event).
5. **Universal Placeholders:** Use `{transportMode}`, `{secondaryExam}`, `{moneyApp}` instead of hardcoded terms.
6. **Valid JSON Syntax:** No trailing commas or malformed quotes (Kotlinx Serialization requires strict JSON).
7. **Verification Test:** Run `./gradlew :app:testDebugUnitTest --tests "com.maisha.game.domain.GlobalContentCoverageTest"` to verify that all event packs parse cleanly and resolve placeholders for all 15 countries.
