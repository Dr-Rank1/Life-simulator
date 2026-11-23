# Privacy Policy — Maisha (Life Simulator)

**Last updated:** 23 November 2026  
**Package:** `com.maisha.game`  
**Repository:** [https://github.com/stewiriffin/Life-simulator](https://github.com/stewiriffin/Life-simulator)

Maisha (“the app”, “we”, “our”) is a single-player, offline-first life simulation mobile game for Android. This Privacy Policy explains what data the app uses, where it is stored, how optional network requests operate, and how your privacy is protected.

---

## 1. Local-First Gameplay & On-Device Storage

Maisha is built on an **offline-first** architectural philosophy. All gameplay rules, calculations, procedural portrait generation, and narrative event packs run locally on your device.

We store the following gameplay data strictly on your device using Android Room Database and Jetpack DataStore:
- **Character Save Slots:** Character attributes (stats, age, gender, country, education history, career state, assets, mortgages, relationship rosters, crime history, prison records) across three local save slots.
- **Dynasty Records & Family Trees:** Ancestry lineages, generation numbers, and inheritance history.
- **Achievements & Badges:** Unlocked trophies, high scores, and tombstone ribbons.
- **Preferences:** Audio volume toggles, haptic vibration preferences, selected language, onboarding status, and gameplay tooltips.

This data never leaves your device unless you choose to share a life summary image or synchronize via Google Play Games Services.

---

## 2. Google Play Games Services v2 (Cloud Saves & Leaderboards)

Maisha integrates optional **Google Play Games Services v2** to enhance player experience:
- **Cloud Snapshots:** If you sign in to Google Play Games, your saved character slots and dynasty progression may be backed up to your personal Google Drive / Play Games cloud storage via `SnapshotsClient`. We do not operate custom backend servers to store or access this data.
- **Global Leaderboards:** Scores for *Net Worth*, *Longest Life*, *Generations Sustained*, and *Peak Happiness* are submitted to official Google Play Games leaderboards. Data submission is governed by Google's Privacy Policy.

You can disconnect or manage your Google Play Games account at any time through the Google Play Games app or your Android system settings.

---

## 3. Optional Public Flavor Data Prefetching

To enrich gameplay with real-world names, news headlines, book titles, and museum exhibits, Maisha may optionally download public, non-personalized data in the background from the following public API endpoints:

| Service | Category / Purpose in Maisha |
|---|---|
| [Advice Slip](https://api.adviceslip.com/) | Parent and mentor advice quotes |
| [Open Trivia DB](https://opentdb.com/) | Academic exam question flavor |
| [NHTSA vPIC](https://vpic.nhtsa.dot.gov/api/) | Authentic vehicle make and model listings |
| [Evil Insult Generator](https://evilinsult.com/) | Street and playground conflict encounter text |
| [Gutendex](https://gutendex.com/) | Public-domain book titles for library reading |
| [ZenQuotes](https://zenquotes.io/) | Meditation and social media post flavor |
| [OpenStreetMap Nominatim](https://nominatim.openstreetmap.org/) | Realistic street names for real estate listings (cached locally per city) |
| [The Met Collection API](https://metmuseum.github.io/) | Museum and art gallery excursion titles |
| [FBI Wanted](https://api.fbi.gov/docs) | Sanitized news and law enforcement flavor |
| [Spaceflight News](https://api.spaceflightnewsapi.net/) | STEM workplace headlines and science news |

### Strict Privacy Protections for Flavor Requests:
- **No Personal Identifiable Information (PII):** These network requests send no account identifiers, character stats, device identifiers, IP geolocation queries, or gameplay state.
- **Locally Cached:** Content is stored locally in Room database cache and reused across sessions.
- **Zero Gameplay Blocking:** Gameplay never stalls waiting for network connectivity. If a request fails or the device is offline, Maisha uses bundled offline text catalogs.

---

## 4. Optional Random Name Generator

During character creation, tapping **Random Name** may optionally request a first and last name from the [Random User Generator](https://randomuser.me/).
- Only gender and nationality parameters (e.g. `nat=us` or `nat=br`) are included in the request.
- No user profile, device ID, or account data is transmitted.
- If the request fails, times out, or the device is offline, Maisha draws names from its bundled offline catalog of thousands of authentic names across 15 nations.

---

## 5. Advertising (Google AdMob)

Maisha may display ads provided by **Google Mobile Ads (AdMob)**:
- **Ad Formats:** Banners on the character slot picker, occasional interstitial ads on age-ups, and optional rewarded video ads for the "Second Wind" revive mechanic upon premature character death.
- **Data Collection by AdMob:** Google AdMob may collect advertising identifiers (e.g., Android Advertising ID), approximate device location, and technical device specifications as outlined in [Google's Privacy & Terms](https://policies.google.com/privacy).
- **User Control:** You can reset or opt out of personalized advertising at any time via Android System Settings (`Settings` &rarr; `Google` &rarr; `Ads`).
- We do not sell, rent, or trade your personal data.

---

## 6. Local Device Notifications

If granted notification permissions on Android 13+ (API 33+), Maisha schedules contextual daily reminders (e.g. reminding you of your saved character's upcoming birthday) using Android's local **WorkManager**.
- Notifications are scheduled and generated 100% locally on the device.
- No external push notification servers or tracking tokens (e.g. FCM) are used.

---

## 7. Life Summary Sharing

When you tap **Share My Life** on the Life Summary or Tombstone screen:
- A graphic summary card is generated entirely in device memory.
- The image is saved temporarily to local device cache and opened via the standard Android System Share Sheet (`Intent.ACTION_SEND`).
- Maisha does not upload, track, or inspect your shared images.

---

## 8. Children’s Privacy

Maisha is designed as a general-audience life simulation game. We do not knowingly solicit or collect personal information from children under 13 (or under the applicable age of digital consent in your jurisdiction). If ads are enabled, AdMob ad requests are tagged with appropriate maximum content ratings.

---

## 9. Changes to This Privacy Policy

We may periodically update this Privacy Policy to reflect codebase enhancements or regulatory adjustments. Any modifications will be posted to this document with an updated revision date.

---

## 10. Contact Information

If you have questions, feedback, or concerns regarding this Privacy Policy or Maisha's data practices, please open an issue on the official GitHub repository:

**Repository:** [https://github.com/stewiriffin/Life-simulator](https://github.com/stewiriffin/Life-simulator)
