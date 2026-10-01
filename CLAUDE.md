# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android app for the 2Coders Studio tech assignment, backed by the TMDB API. It has three screens: popular movies (infinite scroll), details, and debounced search over movies or TV series. It's a single `:app` module that follows Clean Architecture, MVVM and the Repository pattern, with Compose, Navigation Compose, Koin and Retrofit. `README.md` explains the design decisions and `docs/runbooks/tmdb.md` covers TMDB specifics. Read both before changing architecture or networking.

Status: the architecture, data layer, ViewModels and tests are done. The screens in `presentation/*/…Screen.kt` are minimal placeholders wired to their ViewModels. The finished UI should replace their bodies but keep their signatures (a ViewModel plus navigation callbacks).

## Commands

```bash
./gradlew assembleDebug                      # build debug APK
./gradlew installDebug                       # install on connected device/emulator
./gradlew testDebugUnitTest                  # all JVM unit tests (app/src/test)
./gradlew lintDebug                          # Android lint
./gradlew connectedAndroidTest               # instrumented tests, needs a device

# single test class / method
./gradlew :app:testDebugUnitTest --tests "com.twocoders.movieapp.presentation.search.SearchViewModelTest"
./gradlew :app:testDebugUnitTest --tests "*.PaginatorTest.calls while a load is in flight are ignored"
```

The build **fails at configuration** if `tmdb.properties` (repo root, gitignored) is missing or lacks `api.key` / `api.read.access.token`. `tmdb.properties.example` is the template.

## Toolchain (bleeding edge, check before assuming APIs)

- AGP 9.4.1 with Gradle 9.6.0. Kotlin support is built into AGP 9, so there is no `kotlin-android` plugin, only the Compose and serialization compiler plugins (Kotlin 2.2.10).
- **Kotlin is pinned at 2.2.10.** Libraries are chosen to match: kotlinx-serialization 1.9.0, coroutines 1.10.2, Koin 4.1.1. Newer releases may need Kotlin 2.3+ metadata, so bump Kotlin first if you upgrade them.
- The daemon JVM is toolchain 25 (foojay). App bytecode targets Java 11. compileSdk/targetSdk are 37 and minSdk is 24.
- The configuration cache is on. Build logic must read files through `providers` (see how `tmdb.properties` is loaded).
- R8 keep rules go in `app/src/main/keepRules/` (the AGP 9 convention). Release optimization is currently off.
- All versions live in `gradle/libs.versions.toml`. Compose, Koin and OkHttp versions come from their BOMs.

## Architecture rules

Layers are packages under `com.twocoders.movieapp`. Dependencies only point inwards:

- `domain/` is **pure Kotlin**. It must not import Android, Retrofit or kotlinx.serialization. For example, the R8 rule for `MediaType` lives in `keepRules/` instead of a `@Keep` annotation. Repositories are interfaces here.
- `data/` implements the repositories. Every network call goes through `ApiCallHandler.execute(request, map)`, which returns `DataResult<T>`. Don't catch exceptions in repositories or ViewModels. Add new failure kinds to the sealed `AppError` instead.
- `TmdbApi` methods return `Response<T>`, so the status is checked before anything is decoded. DTO lists default to `emptyList()`, and the shared `TmdbJson` (`coerceInputValues`, `explicitNulls = false`) handles TMDB's nulls.
- DTOs (`data/remote/dto`) never leave the data layer. Mappers in `data/mapper` produce domain models, and `ImageUrlBuilder` builds image URLs.
- `presentation/` has one package per screen (ViewModel, UI state, Screen). Each ViewModel exposes a single `StateFlow`. Screen content and operation status are separate types: for example `PaginationState.items` vs the sealed `PaginationStatus`, or the sealed `DetailsUiState`.
- Lists paginate with `presentation/paging/Paginator`. It's a hand-written state machine, not Paging 3. Use it for any new paginated list.
- Screens get the ViewModel and navigation callbacks, never the `NavController`. Routes are `@Serializable` types in `presentation/navigation/Routes.kt`. Route args reach ViewModels through Koin `parametersOf`, not `SavedStateHandle`.
- DI has one Koin module per layer in `di/`, all listed in `appModules`. When you add a binding, `di/AppModulesTest` should resolve it.
- Package names mirror directories.

## Testing conventions

- Use hand-written fakes in `app/src/test/.../fakes` (with `TestData` builders) rather than mocks.
- Data-layer tests use `TmdbServerRule`, which runs MockWebServer with the real Retrofit + `TmdbJson` stack.
- ViewModel tests use `testutil/MainDispatcherRule` (StandardTestDispatcher). `runTest` shares its scheduler, so `advanceTimeBy` also drives debounce timers.
- Don't pass `backgroundScope` to a `Paginator` in tests: `advanceUntilIdle()` doesn't run background tasks. Pass the `TestScope` instead.

## Git

- Work happens on feature branches, and the user merges them into `main` themselves.
- Commits use the repo-local identity that's already configured.
- **Commit messages must not contain `Co-Authored-By` or `Claude-Session` trailers.**
