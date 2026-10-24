# AGENTS.md — Developer & AI Agent Guide for Procrastaint

Welcome to **Procrastaint**. This document serves as the authoritative, in-depth architectural and implementation guide for autonomous agents, AI assistants, and human engineers working in this repository.

This guide specifically details the project architecture, dependencies, code conventions, and provides the complete technical specification for implementing **User-Configurable Text Parsing Settings** via the [KWhen](https://github.com/Pahina0/KWhen) library.

---

## 1. Project Overview & Architecture

**Procrastaint** is a smart, modern task management application built using **Kotlin Multiplatform (KMP)**. Its core differentiator is seamless natural language processing: users type tasks in free-form English (e.g. `"Meeting tomorrow morning"`, `"Gym every other Tuesday at 5pm #health"`), and the app automatically extracts dates, times, repetition rules, and tags while stripping them from the task title.

### Technology Stack
* **Language & Runtime:** Kotlin Multiplatform (Common, Android, iOS targets).
* **UI Framework:** Jetpack Compose (Android) with Material 3 / Material Expressive design tokens. (iOS has a SwiftUI frontend in `iosApp/`).
* **Dependency Injection:** [Koin](https://insert-koin.io/) (`org.koin:koin-core`, `koin-android`, `koin-androidx-compose`, `koin-compose`).
* **Local Persistence:**
  * **Database:** AndroidX Room (`androidx.room`) with SQLite bundled driver (`BundledSQLiteDriver`) across KMP.
  * **Key-Value / Settings:** AndroidX DataStore Preferences (`androidx.datastore:datastore-preferences-core`).
* **Network & Sync:** Ktor 3.x, Ktorfit 2.x, Google Calendar API OAuth2 synchronization.
* **Date & Time:** `kotlinx.datetime` and `kotlin.time`.
* **Natural Language Parsing:** [KWhen](https://github.com/Pahina0/KWhen) (`io.github.pahinaa.kwhen:kwhen`).
* **Code Quality & Linting:** Detekt (`./gradlew detektall`).

### High-Level Directory Structure
```
Procrastaint/
├── androidApp/                                  # Android application module
│   └── src/main/java/ap/panini/procrastaint/
│       ├── ui/
│       │   ├── MainActivityViewModel.kt         # Manages active task input and parser invocation
│       │   ├── navigation/                      # Navigation routes and NavHost graph
│       │   └── settings/                        # Settings screens and section components
│       │       ├── SettingsScreen.kt            # Main settings composable
│       │       ├── SettingsViewModel.kt         # Settings state holder
│       │       ├── components/                  # Reusable SettingsItem, Section, SectionHeader
│       │       └── groups/                      # Settings sections (About, Sync, etc.)
├── shared/                                      # Kotlin Multiplatform shared module
│   └── src/
│       ├── commonMain/kotlin/ap/panini/procrastaint/
│       │   ├── data/
│       │   │   ├── database/                    # Room DB, DAOs, migrations
│       │   │   ├── entities/                    # Task, Tag, and sync entities
│       │   │   └── repositories/
│       │   │       ├── PreferenceRepository.kt  # DataStore preferences management
│       │   │       └── TaskRepository.kt        # Task data access and synchronization
│       │   ├── di/
│       │   │   └── AppModule.kt                 # Koin dependency injection module
│       │   └── util/
│       │       ├── Parser.kt                    # Wraps KWhen TimeParser and extracts tags
│       │       └── Time.kt                      # Time mappings and conversions
├── iosApp/                                      # iOS SwiftUI application
├── gradle/
│   └── libs.versions.toml                       # Dependency version catalog
└── config/
    └── detekt.yml                               # Detekt static analysis rules
```

---

## 2. KWhen Library & Parsing Integration

### What is KWhen?
[KWhen](https://github.com/Pahina0/KWhen) is an open-source natural language time/date extraction library developed specifically for Kotlin Multiplatform by Pahina0. It takes arbitrary text and detects relative and absolute temporal expressions, recurring frequencies, intervals, and ambiguous time keywords.

### The KWhen Configuration Model: `ENConfig`
Parsing behavior in KWhen is driven by `ap.panini.kwhen.configs.ENConfig`. The configuration parameters define how ambiguous keywords and dates resolve:

```kotlin
package ap.panini.kwhen.configs

import ap.panini.kwhen.common.Controller
import ap.panini.kwhen.en.ENController
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime

data class ENConfig @OptIn(ExperimentalTime::class) constructor(
    val evening: Int = 18,            // Default: 18 (6:00 PM) - hour for "evening"
    val morning: Int = 9,             // Default: 9  (9:00 AM) - hour for "morning"
    val afternoon: Int = 15,          // Default: 15 (3:00 PM) - hour for "afternoon"
    val night: Int = 20,              // Default: 20 (8:00 PM) - hour for "night"
    val use24: Boolean = false,       // Default: false - parse/format 24-hour times
    val useEuropeanDate: Boolean = false, // Default: false - DD/MM/YYYY vs MM/DD/YYYY
    override val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    override val relativeTo: Long = Clock.System.now().toEpochMilliseconds(),
) : Config() {
    override fun instance(): Controller = ENController(this)
}
```

### Current Parser Implementation in Procrastaint
In [`shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Parser.kt`](file:///Users/pahina/Documents/Procrastaint/shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Parser.kt):
```kotlin
class Parser(private val db: TaskRepository) {
    private var textParser = TimeParser() // Uses default uncustomized ENConfig()

    private fun reset() {
        textParser = TimeParser()
    }

    fun parse(input: String): Parsed {
        reset()
        // Extracts tags (#tag) and calls textParser.parse(input)
        ...
    }
}
```

Currently, `Parser` only receives `TaskRepository` and instantiates `TimeParser` with hardcoded defaults. Users cannot adjust what hour "morning" or "evening" defaults to, nor can they switch to European date notation (`DD/MM/YYYY`) or 24-hour time.

---

## 3. Feature Specification: KWhen Parser Settings

### Objective
Allow users to customize the KWhen parser settings from the app's Settings interface. Changes made in the settings must be persisted via DataStore, reactively propagated to `Parser`, and take immediate effect when parsing task input.

### Configurable Preferences
| Setting Key | Data Type | Default | Description |
|---|---|---|---|
| `kwhen_morning_hour` | `Int` | `9` (9:00 AM) | Hour of the day representing "morning" (0–23) |
| `kwhen_afternoon_hour` | `Int` | `15` (3:00 PM) | Hour of the day representing "afternoon" (0–23) |
| `kwhen_evening_hour` | `Int` | `18` (6:00 PM) | Hour of the day representing "evening" (0–23) |
| `kwhen_night_hour` | `Int` | `20` (8:00 PM) | Hour of the day representing "night" (0–23) |
| `kwhen_use_24` | `Boolean` | `false` | Whether to prefer 24-hour notation for ambiguous times |
| `kwhen_use_european_date`| `Boolean` | `false` | When true, numeric dates parse as `DD/MM/YYYY`; otherwise `MM/DD/YYYY` |

---

## 4. Architectural Design & Data Flow

```mermaid
flowchart TD
    subgraph UI ["UI Layer (androidApp)"]
        SettingsScreen["SettingsScreen.kt"]
        ParserSettingsScreen["ParserSettingsScreen / Section"]
        LivePreview["Interactive Live Preview Component"]
        ViewModel["SettingsViewModel / ParserSettingsViewModel"]
        MainVM["MainActivityViewModel.kt"]
    end

    subgraph Data ["Data & Storage Layer (shared)"]
        PrefRepo["PreferenceRepository.kt (DataStore Preferences)"]
        DataStore[("androidx.datastore")]
    end

    subgraph Engine ["Domain / Parsing Engine (shared)"]
        Parser["Parser.kt"]
        KWhen["TimeParser (KWhen: ENConfig)"]
    end

    SettingsScreen --> ParserSettingsScreen
    ParserSettingsScreen --> LivePreview
    ParserSettingsScreen --> ViewModel
    ViewModel -->|Read / Write Settings| PrefRepo
    PrefRepo <-->|Persist Keys| DataStore

    PrefRepo -->|Flow<ENConfig>| Parser
    Parser --> KWhen
    MainVM -->|Calls parse() with user config| Parser
```

### Detailed Component Roles

#### 1. Preference Persistence ([`PreferenceRepository.kt`](file:///Users/pahina/Documents/Procrastaint/shared/src/commonMain/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepository.kt))
* Add DataStore keys:
  ```kotlin
  const val KWHEN_MORNING_HOUR = "kwhen_morning_hour"
  const val KWHEN_AFTERNOON_HOUR = "kwhen_afternoon_hour"
  const val KWHEN_EVENING_HOUR = "kwhen_evening_hour"
  const val KWHEN_NIGHT_HOUR = "kwhen_night_hour"
  const val KWHEN_USE_24 = "kwhen_use_24"
  const val KWHEN_USE_EUROPEAN_DATE = "kwhen_use_european_date"
  ```
* Provide reactive flow of configuration:
  ```kotlin
  fun getKwhenConfig(): Flow<ENConfig>
  ```
  This combines the preference flows and yields an `ENConfig` with the user's saved hours, flags, system timezone, and current clock reference.
* Provide granular getters and setters:
  * `setMorningHour(hour: Int)`
  * `setAfternoonHour(hour: Int)`
  * `setEveningHour(hour: Int)`
  * `setNightHour(hour: Int)`
  * `setUse24Hour(use24: Boolean)`
  * `setUseEuropeanDate(useEuropean: Boolean)`
  * `resetKwhenConfigToDefaults()`

#### 2. Parsing Engine Update ([`Parser.kt`](file:///Users/pahina/Documents/Procrastaint/shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Parser.kt))
* Inject or observe `PreferenceRepository` (or pass `ENConfig` dynamically).
* In `reset()`: initialize `TimeParser(config)` with the latest `ENConfig`.
* Ensure thread-safety and non-blocking reads (e.g., maintaining an in-memory cached `ENConfig` updated via coroutine collection).

#### 3. Dependency Injection ([`AppModule.kt`](file:///Users/pahina/Documents/Procrastaint/shared/src/commonMain/kotlin/ap/panini/procrastaint/di/AppModule.kt))
* Update `Parser` definition:
  ```kotlin
  single { Parser(get(), get()) } // TaskRepository and PreferenceRepository
  ```
  *(Note: Evaluate whether `single` or `factory` is optimal once `Parser` holds reactive state).*

#### 4. Settings UI in Android ([`androidApp/src/main/java/ap/panini/procrastaint/ui/settings/`](file:///Users/pahina/Documents/Procrastaint/androidApp/src/main/java/ap/panini/procrastaint/ui/settings/))
* **Navigation Route:** Add a new route in [`Route.kt`](file:///Users/pahina/Documents/Procrastaint/androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/Route.kt):
  ```kotlin
  @Serializable
  data object ParserSettings : Route
  ```
* **Settings Item Entry:** In `SettingsScreen.kt`, add a new section or item:
  * Icon: `Icons.Outlined.Schedule` / `Icons.Outlined.Tune` / `Icons.Outlined.Spellcheck`
  * Title: `"Date & Time Parsing"` or `"Natural Language Settings"`
  * Subtext: `"Configure keywords, time thresholds, and date formats"`
  * Trailing item: chevron navigating to `Route.ParserSettings`.
* **Parser Settings Screen / Content:**
  * **Time of Day Hours Section:**
    * Morning (default 9:00 AM)
    * Afternoon (default 3:00 PM)
    * Evening (default 6:00 PM)
    * Night (default 8:00 PM)
    * Control type: Material 3 `TimePickerDialog` or accessible slider/picker formatted with user's preferred time display (12h or 24h).
  * **Date & Formatting Switches:**
    * **European Date Format (`DD/MM/YYYY`)**: Switch toggle with explanatory subtext (`"e.g. 03/04 resolves to April 3rd instead of March 4th"`).
    * **24-Hour Time Format**: Switch toggle (`"Assume 24-hour format for ambiguous clock numbers"`).
  * **Interactive Live Preview / Test Sandbox:**
    * A small card containing an editable text field (seeded with sample phrases like `"Do laundry tomorrow morning"`, `"Doctor visit 04/05 at 4"`) and real-time badges showing the parsed outcome.
    * Allows the user to verify immediately how changes in settings affect parse results.
  * **Reset to Defaults Action:**
    * Button or App Bar menu item to restore initial settings (`9`, `15`, `18`, `20`, `false`, `false`).

---

## 5. Coding Standards & Agent Guidelines

1. **Strict KMP Constraint:**
   * Never import `java.time.*`, `java.util.Date`, or `android.*` inside `shared/src/commonMain`.
   * Use `kotlinx.datetime.*` and `kotlin.time.*` for all timestamps, durations, and time zones.
2. **UI & Compose Guidelines:**
   * Maintain Material 3 styling consistency. Use existing components from [`ui/settings/components/`](file:///Users/pahina/Documents/Procrastaint/androidApp/src/main/java/ap/panini/procrastaint/ui/settings/components/) (`SettingsItem`, `Section`, `SectionHeader`).
   * Keep Composables stateless where appropriate; hoist state to the ViewModel.
   * Annotate UI state models with `@Immutable`.
3. **Coroutines & Flow:**
   * Collect flows lifecycle-aware in Compose via `collectAsStateWithLifecycle()`.
   * Perform asynchronous DataStore edits inside `viewModelScope.launch`.
4. **Detekt & Formatting:**
   * Always run `./gradlew detektall` before committing changes to ensure adherence to formatting rules.
5. **Preserve Existing Documentation:**
   * Do not delete or clobber unrelated comments, docstrings, or license headers.
6. **Strict No-Emoji Policy:**
   * ABSOLUTELY DO NOT USE EMOJIS anywhere in the app UI, components, vector fallbacks, notifications, logs, or documentation. Use official Material Icons (`Icons.Outlined.*`, `Icons.Filled.*`) or clear text descriptions instead.
