# MovieApp — 2Coders Studio tech assignment

An Android app that browses movies and TV shows from [The Movie Database (TMDB)](https://developer.themoviedb.org/docs/getting-started). It has three screens:

| Screen | What it does |
|---|---|
| **Movies** | Endless list of popular movies, each showing a title, poster and short description |
| **Details** | Extended information: rating, vote count, genres, runtime/seasons, directors/creators, writers and cast |
| **Search** | Debounced search over **movies or TV series**, chosen before searching, with paginated results |

Built with Kotlin, Jetpack Compose, Navigation Compose, Koin, Retrofit, kotlinx.serialization and coroutines/Flow.

---

## Setup

**Requirements:** Android Studio with AGP 9.4 support, and a device or emulator on API 24+. Gradle provisions JDK 25 for the daemon automatically through foojay.

1. **Get TMDB credentials.** Sign in at themoviedb.org → *Settings → API*, and copy both the **API Key** and the **API Read Access Token**.
2. **Create `tmdb.properties`** in the project root. Start from the template:
   ```bash
   cp tmdb.properties.example tmdb.properties
   ```
   ```properties
   api.key=<your API key>
   api.read.access.token=<your read access token>
   ```
   The file is gitignored. If it's missing or incomplete, the build fails with a message that names the missing key. You get a clear error at build time instead of an app that silently returns 401 on every request.
3. **Build and run.**
   ```bash
   ./gradlew installDebug
   ```

### Useful commands

```bash
./gradlew testDebugUnitTest      # all JVM unit tests
./gradlew :app:testDebugUnitTest --tests "*.SearchViewModelTest"   # one class
./gradlew lintDebug              # Android lint
./gradlew assembleDebug          # build the APK
```

---

## Architecture

Clean Architecture in one `:app` module. The layers are packages, and dependencies only point inwards:

```
 presentation ──► domain ◄── data
 (Compose, VMs)   (pure Kotlin)   (Retrofit, DTOs)
        ▲                            ▲
        └──────────── di (Koin) ─────┘
```

```
com.twocoders.movieapp
├── core/logging        Logger interface + Android implementation
├── domain              no Android, Retrofit or serialization imports
│   ├── model           MediaSummary, MediaDetails (sealed), Credits, Page<T>, …
│   ├── error           AppError (sealed), DataResult<T>
│   ├── repository      repository interfaces
│   └── usecase         GetPopularMovies, GetMediaDetails, SearchMedia
├── data
│   ├── remote          TmdbApi, AuthInterceptor, ApiCallHandler, TmdbJson, dto/
│   ├── mapper          DTO → domain, image URLs
│   └── repository      repository implementations
├── presentation
│   ├── paging          PaginationState (state machine) + Paginator
│   ├── movies | details | search   one package per screen: ViewModel, UI state, Screen
│   ├── navigation      type-safe routes + AppNavHost
│   └── ui/theme
├── di                  Koin modules (core, network, data, domain, presentation)
├── application/MovieApp.kt   Application, starts Koin
└── MainActivity.kt     single Activity
```

### How a request flows

`MovieListScreen` → `MovieListViewModel` → `Paginator` → `GetPopularMoviesUseCase` → `MovieRepository` (interface) → `MovieRepositoryImpl` → `ApiCallHandler` + `TmdbApi` → mapped back to `DataResult<Page<MediaSummary>>` → `PaginationState` → a `StateFlow` the screen collects.

---

## Design decisions

### MVVM with state machines
Every screen exposes **one `StateFlow`** of immutable state, and the UI is a function of that state.

- **Lists** use `PaginationState<T>`, which has two parts: the screen content (`items`) and the state of the load operation (`status`, a sealed `PaginationStatus`).
  - `status` moves through `Idle → LoadingFirstPage/LoadingNextPage → Idle | EndReached | Error`.
  - Because content and operation state are separate, the UI can keep showing loaded items while a footer shows *loading more* or *retry*.
  - The transitions are pure functions, so they're unit-tested directly. A diagram is in the KDoc of `PaginationState`.
- **Details** uses a sealed `DetailsUiState`: `Loading`, `Content` or `Error`.
- **Search** uses `SearchUiState`, made of the query text, the media type, and a sealed `SearchResults` (`Idle` or `Content`).

### Pagination without Paging 3
The assignment asks for state machines, and owning the paginator keeps that logic visible and testable. Paging 3 would hide load state inside `LazyPagingItems`. `Paginator` is about 70 lines and handles the cases that matter:

- duplicate scroll triggers are ignored
- after an error, loading stops until the user retries; it doesn't retry by itself
- a refresh cancels the load in flight and drops its result
- items are de-duplicated across pages, because TMDB rankings shift between requests and a repeated id would crash a keyed `LazyColumn`

### Throttled search
`SearchViewModel` debounces the query by 400 ms, then trims it, combines it with the media type, and applies `distinctUntilChanged` and `flatMapLatest`.

- Only typing is debounced. Switching between movies and TV series searches immediately, and clearing the field resets right away.
- `flatMapLatest` cancels the request for an outdated query, so a slow old response can never replace newer results.
- Each search gets its own `Paginator`, so search results scroll endlessly too.

### Data model
- `MediaSummary` is the one list-item model for both movies and TV shows. The mappers hide TMDB's different field names (`title`/`name`, `release_date`/`first_air_date`).
- `MediaDetails` is a **sealed interface** with two variants, `MovieDetails` (runtime, budget, revenue) and `TvShowDetails` (seasons, episodes). The UI renders the shared fields once and uses an exhaustive `when` for the extras.
- `Credits` arrives already split into cast, directors and writers. For TV shows, the creators take the place of directors.
- The data layer normalises unknown values to `null`: TMDB sends `0` for an unknown budget or revenue, and blank strings for a missing tagline or date.

### Error handling
`ApiCallHandler` is the single place where Retrofit outcomes become a `DataResult<T>`:

1. It checks the HTTP status first. The body is decoded only for 2xx responses.
2. For a non-2xx response, it reads TMDB's error body (`status_code`, `status_message`) and maps it to a sealed `AppError`. The server's message is kept, so the UI can show something useful:
   - 401 → `Unauthorized`
   - 404 → `NotFound`
   - other codes → `Http(code, message)`
3. A failed connection becomes `NoConnection`, an undecodable body becomes `Parsing`, and anything else becomes `Unknown`.
4. Full details go to the log through `Logger`. Only the typed error reaches the UI.
5. `CancellationException` is always rethrown, so cancelling a coroutine (for example through `flatMapLatest`) keeps working.

Repositories and ViewModels never catch exceptions. They `when` over `DataResult` and `AppError`.

TMDB sometimes sends `null` where a list is expected. `TmdbJson` sets `coerceInputValues = true` and `explicitNulls = false`, and every DTO list defaults to `emptyList()`, so those `null`s decode to empty lists.

### Dependency injection (Koin)
- There's one module per layer, collected in `appModules`.
- Repositories are bound to their domain interfaces, so presentation and domain never see an implementation.
- `DetailsViewModel` gets its route arguments through `parametersOf` instead of `SavedStateHandle`. It stays a plain class that tests can construct directly.
- `AppModulesTest` starts the real graph and resolves every entry point, so a broken binding fails in CI instead of at launch.

### Navigation
- Navigation Compose uses type-safe `@Serializable` routes, and `DetailsRoute(id, type)` carries typed arguments.
- Screens receive callbacks instead of the `NavController`, which keeps them independent and previewable.
- There's a single Activity.

### Security
- Credentials live only in the gitignored `tmdb.properties` and reach the app through `BuildConfig`.
- The read access token is sent as a `Bearer` header, so it's never in URLs.
- The OkHttp logger runs in debug builds only, and always redacts `Authorization`.

---

## Testing

There are 50 JVM unit tests under `app/src/test`. Their packages mirror the main source set:

| Layer | What's covered | How |
|---|---|---|
| domain | use cases: dispatch by type, blank-query short-circuit, trimming | hand-written fake repositories (`fakes/`) |
| data | every `AppError` mapping, the auth header, `null` handling, mapping (credits, image URLs, 500-page clamp), endpoint paths | **MockWebServer** with the real Retrofit + `TmdbJson` stack (`TmdbServerRule`) |
| presentation | pagination state transitions, `Paginator` concurrency, each ViewModel; for search: debounce, cancelling stale requests, type switch | fakes + `MainDispatcherRule` + virtual time (`advanceTimeBy`), Turbine for emission order |
| di | the full Koin graph resolves | `KoinTestRule` |

Fakes are preferred over mocks. They record their calls and answer from a lambda the test can swap, which keeps assertions readable.

---

## Further documentation

- [`docs/runbooks/tmdb.md`](docs/runbooks/tmdb.md): TMDB integration runbook (credentials, gotchas, common errors).
- KDoc on public types explains the reasoning behind decisions, not only what the code does.
