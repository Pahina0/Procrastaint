# Design Document: KWhen Text Parser Settings in Procrastaint

**Date:** 2026-10-23  
**Status:** Ready for Review  
**Author:** AI Pair Programmer & Pahina0  

---

## 1. Intent & The "Why"

Procrastaint differentiates itself from standard todo applications through seamless natural language processing: users create time-based tasks simply by typing free-form sentences (e.g. `"Meeting tomorrow morning"`, `"Gym every other Tuesday at 5pm #health"`). This relies on the [KWhen](https://github.com/Pahina0/KWhen) NLP library.

Currently, Procrastaint initializes KWhen with fixed, unconfigurable defaults (9 AM morning, 3 PM afternoon, 6 PM evening, 8 PM night, 12-hour clock, American `MM/DD/YYYY` dates). However, schedules and regional conventions vary widely across individuals (e.g. night shift workers, European date conventions, 24-hour time preferences).

The goal of this project is to provide a dedicated, user-friendly settings screen in Android allowing users to configure KWhen's core natural language parser parameters, backed by a real-time interactive sandbox where they can immediately verify and feel confident in their changes.

---

## 2. Goals, Non-Goals, & Anti-Goals

### Goals
- **Core KWhen Config Customization:** Allow modifying KWhen's 6 core parameters:
  - Morning hour (0–23, default 9)
  - Afternoon hour (0–23, default 15)
  - Evening hour (0–23, default 18)
  - Night hour (0–23, default 20)
  - European Date ordering boolean (`DD/MM/YYYY` vs `MM/DD/YYYY`, default false)
  - 24-Hour Time preference boolean (`ENConfig.use24`, default false)
- **Schedule Freedom:** Allow users complete flexibility to set any hour for any keyword without artificial chronological constraints (e.g. Morning does not have to precede Afternoon).
- **Interactive Verification Sandbox (Option A):** Embed a live sandbox test card on the settings screen showing immediate parsed results (date, time, repeat frequency, cleaned title) as settings are adjusted.
- **Persistent Reactive Storage:** Persist preferences in AndroidX DataStore via `PreferenceRepository` in the `shared` module, reactively propagating changes across the application.
- **Instant Non-Blocking Parsing:** Ensure `Parser.parse()` remains zero-latency and synchronous on every keystroke during normal task input and inside the sandbox.
- **Reset to Defaults:** Offer an explicit "Reset to defaults" action in the top app bar to restore standard initial parameters (9, 15, 18, 20, false, false).
- **iOS Target Preservation:** Maintain clean compilation and source compatibility for the iOS target in `shared` (providing default parameters so `Parser(db)` calls in `iosApp` continue to compile).

### Non-Goals
- **iOS UI:** Implementing SwiftUI screens or controls in `iosApp` is explicitly out of scope for this task (strictly Android UI).
- **App-Wide Time Display Formatting:** The "24-Hour Time" toggle strictly configures parser disambiguation heuristics (`use24` in KWhen) and does not modify general display formatting in task cards, calendar views, or widgets.
- Adding support for other languages (KWhen's `ENConfig` is the sole target).
- Exposing non-core internal engine parameters (mergers, token scoring weights, custom regex patterns).

### Anti-Goals
- Requiring users to exit the settings screen and guess in the task creation sheet to know if their settings worked.
- Forcing strict chronological validation (e.g. blocking a night-shift worker from setting morning to 8 PM).
- Blocking the UI thread with synchronous DataStore disk I/O when typing tasks.

---

## 3. Constraints

- **Platform Scope:** Android-only UI (`androidApp`).
- **Kotlin Multiplatform (KMP) Purity:** In `shared/src/commonMain`, never import `java.time.*` or Android platform APIs. Use `kotlinx.datetime` and `kotlin.time`.
- **UI Framework & Design System:** Android UI must follow Material 3 Expressive guidelines using Jetpack Compose, matching existing components in `ap.panini.procrastaint.ui.settings.components`.
- **Code Quality:** Must pass `./gradlew detektall` cleanly without disabling lint rules or introducing formatting violations.

---

## 4. Approaches Considered

### Approach 1: Reactive In-Memory Cache in `Parser` (Selected)
- **Description:** Inject `PreferenceRepository` into `Parser`. `PreferenceRepository` exposes `getKwhenConfig(): Flow<ENConfig>` combining the six keys. `Parser` collects this flow in an internal coroutine scope into an in-memory `cachedConfig`. In `Parser.reset()`, `TimeParser(cachedConfig)` is instantiated.
- **Tradeoffs:** Zero API changes for callers ([`MainActivityViewModel`](file:///Users/pahina/Documents/Procrastaint/androidApp/src/main/java/ap/panini/procrastaint/ui/MainActivityViewModel.kt), [`DemoPage`](file:///Users/pahina/Documents/Procrastaint/androidApp/src/main/java/ap/panini/procrastaint/ui/onboarding/pages/DemoPage.kt), [`TryPage`](file:///Users/pahina/Documents/Procrastaint/androidApp/src/main/java/ap/panini/procrastaint/ui/onboarding/pages/TryPage.kt)). Calling `parser.parse(text)` remains completely synchronous and zero-latency on every keystroke. Changes in DataStore automatically take effect across the entire app.
- **Decision:** Chosen because it provides optimal separation of concerns, zero caller churn, and immediate reactive consistency.

### Approach 2: Explicit Config Passing on `parse()`
- **Description:** Keep `Parser` stateless and pass `config: ENConfig` to `parser.parse(input, config)`.
- **Tradeoffs:** Keeps `Parser` free of coroutines, but requires every ViewModel in the app to collect preference flows and pass config on every single keystroke.
- **Decision:** Rejected due to excessive boilerplate and tight coupling across ViewModels.

### Approach 3: Global In-Memory Singleton Provider
- **Description:** Maintain a mutable global `KWhenConfigProvider` singleton updated by `PreferenceRepository`.
- **Tradeoffs:** Avoids coroutines in `Parser`, but introduces global mutable singleton state and risks desynchronization between persistent storage and memory.
- **Decision:** Rejected as unidiomatic and error-prone.

---

## 5. Detailed Design & Architecture

```mermaid
flowchart TD
    subgraph UI ["UI Layer (androidApp)"]
        SettingsScreen["SettingsScreen.kt"]
        ParserSettingsScreen["ParserSettingsScreen.kt"]
        Sandbox["Live Parser Sandbox Component"]
        VM["ParserSettingsViewModel.kt"]
        MainVM["MainActivityViewModel.kt"]
    end

    subgraph Shared ["Shared Module (shared)"]
        PrefRepo["PreferenceRepository.kt (DataStore)"]
        Parser["Parser.kt (In-Memory Cache)"]
        TimeParser["TimeParser (KWhen: ENConfig)"]
        Koin["AppModule.kt"]
    end

    SettingsScreen -->|Navigate| ParserSettingsScreen
    ParserSettingsScreen --> Sandbox
    ParserSettingsScreen --> VM
    VM -->|Read / Write Settings| PrefRepo
    PrefRepo -->|Flow<ENConfig>| Parser
    Parser --> TimeParser
    MainVM -->|Calls parse()| Parser
    VM -->|Calls parse() for preview| Parser
```

### 5.1 Data Layer (`PreferenceRepository.kt`)
Add DataStore preference keys:
```kotlin
const val KWHEN_MORNING_HOUR = "kwhen_morning_hour"
const val KWHEN_AFTERNOON_HOUR = "kwhen_afternoon_hour"
const val KWHEN_EVENING_HOUR = "kwhen_evening_hour"
const val KWHEN_NIGHT_HOUR = "kwhen_night_hour"
const val KWHEN_USE_24 = "kwhen_use_24"
const val KWHEN_USE_EUROPEAN_DATE = "kwhen_use_european_date"
```
Provide default values:
- `KWHEN_MORNING_HOUR`: 9
- `KWHEN_AFTERNOON_HOUR`: 15
- `KWHEN_EVENING_HOUR`: 18
- `KWHEN_NIGHT_HOUR`: 20
- `KWHEN_USE_24`: false
- `KWHEN_USE_EUROPEAN_DATE`: false

Provide methods:
- `getKwhenConfig(): Flow<ENConfig>`: Combines all six preference keys, returning an `ENConfig` with the user's values, system default timezone, and current epoch millisecond anchor.
- Granular setters: `setMorningHour(hour: Int)`, `setAfternoonHour(hour: Int)`, `setEveningHour(hour: Int)`, `setNightHour(hour: Int)`, `setUse24(use24: Boolean)`, `setUseEuropeanDate(useEuropean: Boolean)`.
- `resetKwhenConfigToDefaults()`: Clears or restores the six keys to defaults.

### 5.2 Domain Layer (`Parser.kt` & `AppModule.kt`)
Update `Parser` constructor with backward-compatible defaults for iOS:
```kotlin
class Parser(
    private val db: TaskRepository,
    private val preferenceRepository: PreferenceRepository? = null,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
)
```
- If `preferenceRepository != null`, collects `preferenceRepository.getKwhenConfig()` in `coroutineScope` to maintain `private var cachedConfig: ENConfig = ENConfig()`.
- In `reset()`, instantiates `textParser = TimeParser(cachedConfig)`.
- In `AppModule.kt`, provide `Parser`:
  ```kotlin
  single { Parser(get(), get()) }
  ```

### 5.3 UI Layer (`androidApp`)
- **Navigation:**
  - Add `data object ParserSettings : Route` in `Route.kt`.
  - Add composable destination in `NavGraph.kt`.
  - Add navigation item in `SettingsScreen.kt` with icon `Icons.Outlined.Tune` or `Icons.Outlined.Spellcheck` under a "Natural Language Parsing" section.
- **ViewModel (`ParserSettingsViewModel.kt`):**
  - Exposes `uiState: StateFlow<ParserSettingsUiState>` containing:
    - `morningHour: Int`
    - `afternoonHour: Int`
    - `eveningHour: Int`
    - `nightHour: Int`
    - `use24: Boolean`
    - `useEuropeanDate: Boolean`
    - `sandboxInput: String`
    - `sandboxParsed: Parsed?`
  - Re-evaluates `sandboxParsed = parser.parse(sandboxInput)` whenever settings change or `sandboxInput` is edited.
  - Functions: `updateMorningHour(hour)`, `updateAfternoonHour(hour)`, `updateEveningHour(hour)`, `updateNightHour(hour)`, `toggleUse24(enabled)`, `toggleUseEuropeanDate(enabled)`, `updateSandboxInput(text)`, `resetToDefaults()`.
- **Screen (`ParserSettingsScreen.kt`):**
  - **TopAppBar:** Back navigation icon, Title "Date & Time Parsing", Action icon/button "Reset to defaults".
  - **Section 1 (Hours):** 2x2 grid or list of cards for Morning, Afternoon, Evening, Night. Tapping an item opens a Material 3 `TimePickerDialog` or hour selector. Displays in 12h (AM/PM) or 24h format depending on `use24`.
  - **Section 2 (Notation Toggles):** Switch controls for "European Date Format" (with subtitle explaining `DD/MM/YYYY` vs `MM/DD/YYYY`) and "24-Hour Time Format" (explaining numeric disambiguation).
  - **Section 3 (Interactive Sandbox Card):** Pinned or prominently placed card with:
    - Editable text field (defaulted to `"Doctor appointment tomorrow evening #health"`).
    - Quick test chips (e.g. `"Gym tomorrow morning"`, `"Flight 04/05 at 8"`, `"Submit tax 10/11"`).
    - Real-time result badges: Extracted Date/Time chip (green), Tags chip (accent), Clean title string (neutral).

---

## 6. What's Left to the Builder

- Exact layout details of the hour picker dialog (using standard M3 `TimePickerDialog` or accessible wheel/number picker).
- Specific styling tokens for sandbox result chips (aligning with `MaterialTheme.colorScheme`).
- Pre-seeded sample strings for the sandbox chips.
