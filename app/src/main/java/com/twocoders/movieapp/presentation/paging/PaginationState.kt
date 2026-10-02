package com.twocoders.movieapp.presentation.paging

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.model.Page

/**
 * State of a paginated list, modelled as a small state machine.
 *
 * [items] is the content shown on screen. [status] is the state of the load operation
 * around it. Keeping them separate lets the UI show existing items together with a
 * "loading more" or "retry" footer.
 *
 * ```
 *            loadNext                    page loaded, more pages
 *   Idle ──────────────► Loading* ─────────────────────────────► Idle
 *     ▲                     │  │       page loaded, last page
 *     │ (refresh)           │  └────────────────────────────────► EndReached
 *     │                     │ failure
 *     │                     ▼
 *     └─────── retry ───── Error
 * ```
 * `Loading*` is [PaginationStatus.LoadingFirstPage] or [PaginationStatus.LoadingNextPage].
 *
 * The transition functions are pure, which keeps the machine easy to unit test.
 * [Paginator] is the only caller.
 */
data class PaginationState<T>(
    val items: List<T> = emptyList(),
    val nextPage: Int = FIRST_PAGE,
    val status: PaginationStatus = PaginationStatus.Idle,
) {
    /** True when loading finished and nothing came back, e.g. a search with no matches. */
    val isEmpty: Boolean get() = status is PaginationStatus.EndReached && items.isEmpty()

    /** Moves to the loading state for [nextPage]. */
    internal fun onLoadStarted(): PaginationState<T> = copy(
        status = if (nextPage == FIRST_PAGE) PaginationStatus.LoadingFirstPage else PaginationStatus.LoadingNextPage,
    )

    /**
     * Appends [page]. Items are de-duplicated by [keyOf] because TMDB rankings can shift
     * between requests, so one title may show up on two consecutive pages. Duplicate keys
     * would crash a keyed `LazyColumn`.
     */
    internal fun onPageLoaded(page: Page<T>, keyOf: (T) -> Any): PaginationState<T> = copy(
        items = (items + page.items).distinctBy(keyOf),
        nextPage = page.page + 1,
        status = if (page.hasNextPage) PaginationStatus.Idle else PaginationStatus.EndReached,
    )

    /** Keeps the items already loaded and records [error]. The same page is retried next. */
    internal fun onLoadFailed(error: AppError): PaginationState<T> = copy(
        status = PaginationStatus.Error(error, isFirstPage = nextPage == FIRST_PAGE),
    )

    companion object {
        const val FIRST_PAGE = 1
    }
}

/** Where the load operation stands. See the state diagram on [PaginationState]. */
sealed interface PaginationStatus {
    /** Ready to load the next page when asked. */
    data object Idle : PaginationStatus

    /** Loading the first page. Nothing to show yet, so the UI shows a full-screen loader. */
    data object LoadingFirstPage : PaginationStatus

    /** Loading a further page while the earlier items stay visible. */
    data object LoadingNextPage : PaginationStatus

    /**
     * The last load failed. [isFirstPage] tells the UI to show a full-screen error
     * instead of a retry footer under the existing items.
     */
    data class Error(val error: AppError, val isFirstPage: Boolean) : PaginationStatus

    /** Every page has been loaded. */
    data object EndReached : PaginationStatus
}
