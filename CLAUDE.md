# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android movie app for the 2Coders Studio tech assignment, backed by the TMDB API (`https://api.themoviedb.org`). It is a single-module (`:app`) Jetpack Compose project, still at the template stage: `MainActivity` only renders a `Greeting` placeholder.

## Commands

```bash
./gradlew assembleDebug                      # build debug APK
./gradlew installDebug                       # install on connected device/emulator
./gradlew test                               # JVM unit tests (app/src/test)
./gradlew connectedAndroidTest               # instrumented tests (app/src/androidTest), needs a device
./gradlew lint                               # Android lint

# single test class / method
./gradlew :app:testDebugUnitTest --tests "com.twocoders.movieapp.ExampleUnitTest"
./gradlew :app:testDebugUnitTest --tests "com.twocoders.movieapp.ExampleUnitTest.addition_isCorrect"
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.twocoders.movieapp.ExampleInstrumentedTest
```

## Toolchain (bleeding edge, check before assuming APIs)

- AGP 9.4.1 with Gradle 9.6.0. Kotlin support is built into AGP 9, so there is no `kotlin-android` plugin, only `org.jetbrains.kotlin.plugin.compose` (Kotlin 2.2.10).
- The Gradle daemon JVM is toolchain 25, provisioned through foojay (`gradle/gradle-daemon-jvm.properties`). App bytecode targets Java 11.
- compileSdk/targetSdk are 37 (declared with the `compileSdk { version = release(37) }` DSL) and minSdk is 24.
- The configuration cache is on (`gradle.properties`). Build logic must stay configuration-cache safe: no eager file reads or `project` access at execution time.
- R8 keep rules go in `app/src/main/keepRules/` (the AGP 9 convention, not `proguard-rules.pro`). Release optimization is currently turned off (`optimization { enable = false }`).
- All dependency versions live in the `gradle/libs.versions.toml` version catalog. Reference them as `libs.*`. Compose versions come from the BOM.

## Architecture

The intended layering follows the package layout under `com.twocoders.movieapp`. So far only `presentation/ui/theme` (the Material3 `MovieAppTheme`) exists. Put new code in matching `presentation` / `domain` / `data` packages.

Package names mirror directories (e.g. `com.twocoders.movieapp.presentation.ui.theme`); keep them in sync when moving files.

## TMDB configuration

- `BASE_URL` is injected through `buildConfigField` in `app/build.gradle.kts` (`buildFeatures { buildConfig = true }` is required — it's off by default in AGP 9). Access it as `BuildConfig.BASE_URL`.
- Credentials (`api.key`, `api.read.access.token`) are in `tmdb.properties` at the repo root. Nothing in the build reads that file yet. Load it in `app/build.gradle.kts` and expose the values as `buildConfigField`s. The file is gitignored; never commit it.
