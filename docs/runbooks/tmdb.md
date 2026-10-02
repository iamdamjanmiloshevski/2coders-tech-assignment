# Runbook: TMDB integration

This runbook covers the app's only third-party integration, The Movie Database API v3.
API reference: https://developer.themoviedb.org/reference

## Access & credentials

| What | Where |
|---|---|
| Account / keys | themoviedb.org → *Settings → API* (free for non-commercial use, available right away) |
| Credentials used | **API Read Access Token**, sent as `Authorization: Bearer <token>`. The API key is also exposed in `BuildConfig` but isn't currently sent. |
| Local config | `tmdb.properties` in the repo root. It's gitignored, and the template is `tmdb.properties.example`. |
| Build wiring | `app/build.gradle.kts` reads the file and creates `BuildConfig.TMDB_ACCESS_TOKEN`, `TMDB_API_KEY`, `BASE_URL` and `IMAGE_BASE_URL`. |

### Required properties

| Key | Becomes | If missing |
|---|---|---|
| `api.key` | `BuildConfig.TMDB_API_KEY` | the build fails and names the key |
| `api.read.access.token` | `BuildConfig.TMDB_ACCESS_TOKEN` | the build fails and names the key |

After you change `tmdb.properties`, **rebuild and reinstall**. The values are compiled into `BuildConfig`, so nothing reloads at runtime.

For CI, write `tmdb.properties` from CI secrets before running Gradle. Never commit it.

### QA onboarding

There's nothing to whitelist. TMDB has no test-user allow list for read-only API access, so any tester with their own (or a shared) `tmdb.properties` can build and run the app.

## Endpoints used

| Feature | Endpoint | Code |
|---|---|---|
| Popular movies | `GET movie/popular?page=` | `TmdbApi.getPopularMovies` |
| Movie details | `GET movie/{id}?append_to_response=credits` | `TmdbApi.getMovieDetails` |
| TV details | `GET tv/{id}?append_to_response=credits` | `TmdbApi.getTvShowDetails` |
| Search | `GET search/movie` / `search/tv` with `query`, `page` | `TmdbApi.searchMovies` / `searchTvShows` |

## Provider gotchas

- **Nulls instead of empty values.** `results`, `crew` and similar lists can come back as `null`. `poster_path` and `backdrop_path` are often `null`. `release_date` can be `""`. These are handled by `TmdbJson` (`coerceInputValues`, `explicitNulls = false`), by DTO defaults, and by the `toYear()` and `ImageUrlBuilder` helpers.
- **`0` means unknown** for `budget`, `revenue` and `runtime`. The mappers turn it into `null`.
- **500-page cap.** List and search endpoints reject `page > 500`, even when `total_pages` says there are more. `toPage()` clamps `totalPages` to `TMDB_MAX_PAGE`.
- **Rankings shift between pages.** The same id can show up on page N and page N+1 of `movie/popular`. `PaginationState.onPageLoaded` de-duplicates by id.
- **Images are on a different host.** Paths like `/abc.jpg` have to be prefixed with `https://image.tmdb.org/t/p/<size>`. `ImageUrlBuilder` uses `w500` for posters, `w780` for backdrops and `w185` for profile photos.
- **TV vs movie field names.** TV uses `name` and `first_air_date` where movies use `title` and `release_date`, and TV credits list creators under `created_by`. All of this is unified in `data/mapper`.
- **Blank search queries** return an error. `SearchMediaUseCase` never sends one.
- **Rate limit** is roughly 50 requests per second per IP. The search debounce (400 ms) keeps the app far below that.

## Common errors

| Symptom | TMDB response | App error | Fix |
|---|---|---|---|
| Every call fails | 401, `status_code: 7`, "Invalid API key" | `AppError.Unauthorized` | Wrong or expired token in `tmdb.properties`. Copy the **Read Access Token** (the long JWT), not the API key, then rebuild. |
| Details screen error for one title | 404, `status_code: 34` | `AppError.NotFound` | The id doesn't exist for that media type. Check that `DetailsRoute.type` matches the item. |
| "No connection" | IOException | `AppError.NoConnection` | Device is offline, or an emulator DNS problem. |
| Parsing failure | 2xx with an unexpected body | `AppError.Parsing` | TMDB changed a field type. Check the Logcat tag `ApiCallHandler` for the full exception. |
| Build fails: "Missing tmdb.properties" | — | — | Create the file (see Setup in the README). |

To debug, filter Logcat by `ApiCallHandler` (errors include the HTTP code, path and TMDB `status_code`) or by `okhttp` (debug builds log full bodies, with `Authorization` redacted).

## Offline cache

TMDB responses are cached in Room (`movie_cache.db`), and images in Coil's disk cache (`cacheDir/image_cache`, 100 MB). The full design is in the README under "Offline support".

| Question | Answer |
|---|---|
| When is the cache used? | Only when a request fails with `NoConnection`. Online, TMDB is always asked first and the cache is refreshed. |
| Why does an error show offline although I saw that screen? | Each list page, details screen and search (query + media type) is cached separately. Opening details from search caches those details too. Something never loaded isn't cached. |
| A server error but no cached data, even though it's cached? | By design. Only connectivity failures fall back to the cache, so 401, 404 and 5xx stay visible. |
| How long is data kept? | Popular pages and details: until overwritten. Search pages: 7 days, pruned whenever a new search is saved. |
| How do I clear it? | *Settings → Apps → MovieApp → Storage → Clear cache* clears images. *Clear storage* clears both. With adb: `adb shell pm clear com.twocoders.movieapp`. |
| How do I inspect it? | Android Studio → *App Inspection → Database Inspector* → `movie_cache.db`. |
| I changed an entity. What now? | Bump `MovieDatabase.version` and commit the new `app/schemas/.../<version>.json`. The cache rebuilds from scratch (`fallbackToDestructiveMigration`). Nothing is migrated, because it's only a cache. |
| How do I test offline? | Airplane mode on a device. In unit tests, `TmdbServerRule.goOffline()` stops MockWebServer. Dropping a single response isn't enough, because OkHttp retries on a pooled connection. |

Logcat tags: `NetworkFirst` for cache read and write failures, `ApiCallHandler` for network errors.

## Code links

- `app/src/main/java/com/twocoders/movieapp/data/remote/`: `TmdbApi`, `AuthInterceptor`, `ApiCallHandler`, `TmdbJson`, `dto/`
- `app/src/main/java/com/twocoders/movieapp/data/mapper/`: DTO → domain mapping and image URLs
- `app/src/main/java/com/twocoders/movieapp/di/NetworkModule.kt`: OkHttp and Retrofit setup
- `app/src/main/java/com/twocoders/movieapp/data/local/`: Room cache (`MovieDatabase`, DAOs, `RoomMediaLocalDataSource`)
- `app/src/main/java/com/twocoders/movieapp/data/repository/NetworkFirst.kt`: the network-first / cache-fallback rule
- `app/src/test/java/com/twocoders/movieapp/data/`: MockWebServer contract tests (`TmdbServerRule`)
