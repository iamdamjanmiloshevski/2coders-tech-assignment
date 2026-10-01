package com.twocoders.movieapp.application

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.twocoders.movieapp.BuildConfig
import com.twocoders.movieapp.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import okio.Path.Companion.toOkioPath

/**
 * Process entry point. Starts Koin with [appModules] and provides Coil's app-wide image loader.
 * Registered in the manifest as `.application.MovieApp`.
 */
class MovieApp : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.NONE)
            androidContext(this@MovieApp)
            modules(appModules)
        }
    }

    /**
     * Coil's app-wide image loader. Coil uses its own OkHttp client instead of the TMDB one,
     * so the API token is never sent to the image CDN.
     *
     * The disk cache is what keeps posters, backdrops and cast photos visible offline. It's
     * explicit and sized so the offline experience doesn't depend on Coil's defaults.
     * TMDB serves images with long-lived `Cache-Control` headers, so cached files stay usable.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, percent = 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve(IMAGE_CACHE_DIR).toOkioPath())
                    .maxSizeBytes(IMAGE_CACHE_MAX_BYTES)
                    .build()
            }
            .build()

    private companion object {
        const val IMAGE_CACHE_DIR = "image_cache"
        const val IMAGE_CACHE_MAX_BYTES = 100L * 1024 * 1024
    }
}
