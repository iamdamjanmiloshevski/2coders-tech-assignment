package com.twocoders.movieapp.data.local

import androidx.room.TypeConverter
import com.twocoders.movieapp.data.local.entity.CreditsJson
import com.twocoders.movieapp.data.local.entity.GenreJson
import kotlinx.serialization.json.Json

/** Room converters for the JSON columns of [com.twocoders.movieapp.data.local.entity.DetailsEntity]. */
class CacheConverters {

    @TypeConverter
    fun genresToJson(genres: List<GenreJson>): String = json.encodeToString(genres)

    @TypeConverter
    fun genresFromJson(value: String): List<GenreJson> = json.decodeFromString(value)

    @TypeConverter
    fun creditsToJson(credits: CreditsJson): String = json.encodeToString(credits)

    @TypeConverter
    fun creditsFromJson(value: String): CreditsJson = json.decodeFromString(value)

    private companion object {
        // Tolerates rows written by an older or newer app version.
        val json = Json { ignoreUnknownKeys = true }
    }
}
