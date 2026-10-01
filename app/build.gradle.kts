import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

/**
 * TMDB credentials live in the gitignored `tmdb.properties` at the repo root
 * (see `tmdb.properties.example` and the README). Read through `providers` so the
 * configuration cache tracks the file. A missing value fails the build loudly instead of
 * producing an app whose every request returns 401.
 */
val tmdbProperties = Properties().apply {
    val contents = providers
        .fileContents(rootProject.layout.projectDirectory.file("tmdb.properties"))
        .asText
        .orNull
        ?: throw GradleException(
            "Missing tmdb.properties in the project root. " +
                "Copy tmdb.properties.example to tmdb.properties and add your TMDB credentials (see README)."
        )
    load(contents.reader())
}

fun tmdbProperty(key: String): String =
    tmdbProperties.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: throw GradleException("tmdb.properties is missing a value for '$key' (see README).")

android {
    namespace = "com.twocoders.movieapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.twocoders.movieapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "BASE_URL", "\"https://api.themoviedb.org/3/\"")
        buildConfigField("String", "IMAGE_BASE_URL", "\"https://image.tmdb.org/t/p/\"")
        buildConfigField("String", "TMDB_API_KEY", "\"${tmdbProperty("api.key")}\"")
        buildConfigField("String", "TMDB_ACCESS_TOKEN", "\"${tmdbProperty("api.read.access.token")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

room {
    // Exported schemas are committed, so every database change shows up in review and migrations can be tested.
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(platform(libs.okhttp.bom))
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(platform(libs.koin.bom))
    testImplementation(libs.koin.test.junit4)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
