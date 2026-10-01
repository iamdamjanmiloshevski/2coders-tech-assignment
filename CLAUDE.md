# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android app for the 2Coders Studio tech assignment, backed by the TMDB API. It has three screens: popular movies (infinite scroll), details, and debounced search over movies or TV series. It's a single `:app` module that follows Clean Architecture, MVVM and the Repository pattern, with Compose, Navigation Compose, Koin and Retrofit. `README.md` explains the design decisions and `docs/runbooks/tmdb.md` covers TMDB specifics. Read both before changing architecture or networking.

Status: the architecture (branch `feature/architecture`) and the Compose UI (branch `feature/ui`) are both done. The README "UI" section explains the visual design.

## Commands

```bash
./gradlew assembleDebug                      # build debug APK
./gradlew installDebug                       # install on connected device/emulator
./gradlew testDebugUnitTest                  # all JVM unit tests (app/src/test)
./gradlew lintDebug                          # Android lint
./gradlew connectedDebugAndroidTest          # Room instrumented tests, needs a device (MIUI phones must allow USB installs)

# single test class / method
./gradlew :app:testDebugUnitTest --tests "com.twocoders.movieapp.presentation.search.SearchViewModelTest"
./gradlew :app:testDebugUnitTest --tests "*.PaginatorTest.calls while a load is in flight are ignored"
```

The build **fails at configuration** if `tmdb.properties` (repo root, gitignored) is missing or lacks `api.key` / `api.read.access.token`. `tmdb.properties.example` is the template.

## Toolchain (bleeding edge, check before assuming APIs)

- AGP 9.4.1 with Gradle 9.6.0. Kotlin support is built into AGP 9, so there is no `kotlin-android` plugin, only the Compose and serialization compiler plugins (Kotlin 2.2.10).
- **Kotlin is pinned at 2.2.10.** Libraries are chosen to match: kotlinx-serialization 1.9.0, coroutines 1.10.2, Koin 4.1.1, Coil 3.4.0. The 2.2 compiler reads metadata up to 2.3, and newer releases (for example Coil 3.5+) pull kotlin-stdlib 2.4 and fail with "incompatible version of Kotlin". Bump Kotlin first if you upgrade them.
- The daemon JVM is toolchain 25 (foojay). App bytecode targets Java 11. compileSdk/targetSdk are 37 and minSdk is 24.
- The configuration cache is on. Build logic must read files through `providers` (see how `tmdb.properties` is loaded).
- R8 keep rules go in `app/src/main/keepRules/` (the AGP 9 convention). Release optimization is currently off.
- All versions live in `gradle/libs.versions.toml`. Compose, Koin and OkHttp versions come from their BOMs.

## Architecture rules

Layers are packages under `com.twocoders.movieapp`. Dependencies only point inwards:

- `domain/` is **pure Kotlin**. It must not import Android, Retrofit or kotlinx.serialization. For example, the R8 rule for `MediaType` lives in `keepRules/` instead of a `@Keep` annotation. Repositories are interfaces here.
- `data/` implements the repositories. Every network call goes through `ApiCallHandler.execute(request, map)`, which returns `DataResult<T>`. Don't catch exceptions in repositories or ViewModels. Add new failure kinds to the sealed `AppError` instead.
- `TmdbApi` methods return `Response<T>`, so the status is checked before anything is decoded. DTO lists default to `emptyList()`, and the shared `TmdbJson` (`coerceInputValues`, `explicitNulls = false`) handles TMDB's nulls.
- **Offline:** repositories wrap every fetch in `NetworkFirst(fetch, saveToCache, loadFromCache)`. Never read the cache before the network, and never fall back for anything but `AppError.NoConnection`.
  - Room stays behind `MediaLocalDataSource`, with entities, DAOs and mappers in `data/local`. Repository tests use `FakeMediaLocalDataSource`.
  - Schema changes: bump `MovieDatabase.version` and commit the new `app/schemas/*.json`. The database is a cache, so a version change rebuilds it.
  - Room queries are tested on a device (`connectedDebugAndroidTest`). The JVM tests don't run Room.
  - Connectivity goes through `ConnectivityObserver`. ViewModels use `reconnections()` to retry, and tests use `FakeConnectivityObserver`.
- DTOs (`data/remote/dto`) never leave the data layer. Mappers in `data/mapper` produce domain models, and `ImageUrlBuilder` builds image URLs.
- `presentation/` has one package per screen (ViewModel, UI state, Screen). Each ViewModel exposes a single `StateFlow`. Screen content and operation status are separate types: for example `PaginationState.items` vs the sealed `PaginationStatus`, or the sealed `DetailsUiState`.
- Lists paginate with `presentation/paging/Paginator`. It's a hand-written state machine, not Paging 3. Use it for any new paginated list.
- Screens get the ViewModel and navigation callbacks, never the `NavController`. Routes are `@Serializable` types in `presentation/navigation/Routes.kt`. Route args reach ViewModels through Koin `parametersOf`, not `SavedStateHandle`.
- **UI conventions:**
  - Each screen is a stateful `XScreen(viewModel, callbacks)` that collects state and delegates to a stateless `XContent(state, callbacks)`.
  - Previews (`@PreviewLightDark`) target `XContent`, using `presentation/common/PreviewData`.
  - Reuse `presentation/common/components`: `PaginatedMediaList` for any paginated media, `PosterImage` for every remote image, and `FullScreenError`/`EmptyState` for those states.
  - Text goes in `strings.xml`. Map errors with `AppError.toMessage()`. Formatting lives in `Formatters.kt`, as pure functions with tests.
  - Colours come only from `MaterialTheme.colorScheme`, which is the brand palette in `ui/theme/Color.kt`. Dynamic colour is off on purpose.
  - Edge-to-edge: list bottom insets go into `contentPadding` (`bottomContentPadding`/`paddingExceptBottom`). Search adds the IME to the Scaffold insets.
  - **Splash:** use the AndroidX SplashScreen API only (`Theme.MovieApp.Starting` and `installSplashScreen()` before `super.onCreate`). Never add a splash Activity or a splash composable.
  - **Portrait:** the activity is locked to portrait, but Android 16+ ignores that on large screens, so keep layouts working in landscape.
  - Icons come from `material-icons-core`. Anything outside that set is a vector drawable in `res/drawable`, so don't add `material-icons-extended`.
- DI has one Koin module per layer in `di/`, all listed in `appModules`. When you add a binding, `di/AppModulesTest` should resolve it. Bindings that need a `Context` (Room, connectivity) are overridden with fakes there.
- Package names mirror directories.

## Testing conventions

- Use hand-written fakes in `app/src/test/.../fakes` (with `TestData` builders) rather than mocks.
- Data-layer tests use `TmdbServerRule`, which runs MockWebServer with the real Retrofit + `TmdbJson` stack. To simulate offline, call `goOffline()`, which stops the server. A single `DISCONNECT_AT_START` gets retried by OkHttp on a pooled connection.
- ViewModel tests use `testutil/MainDispatcherRule` (StandardTestDispatcher). `runTest` shares its scheduler, so `advanceTimeBy` also drives debounce timers.
- Don't pass `backgroundScope` to a `Paginator` in tests: `advanceUntilIdle()` doesn't run background tasks. Pass the `TestScope` instead.

## Git

- Work happens on feature branches, and the user merges them into `main` themselves.
- Commits use the repo-local identity that's already configured.
- **Commit messages must not contain `Co-Authored-By` or `Claude-Session` trailers.**
