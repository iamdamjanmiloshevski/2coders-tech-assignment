package com.twocoders.movieapp.domain.model

/** Someone who worked on a title, e.g. an actor, a director or a writer. */
data class Person(
    val id: Int,
    val name: String,
    /** Character name for cast, job title (e.g. "Director") for crew. */
    val role: String,
    val profileUrl: String?,
)

/** The people behind a title, already split into the groups the details screen shows. */
data class Credits(
    val cast: List<Person>,
    /** Directors for movies, creators for TV shows. */
    val directors: List<Person>,
    val writers: List<Person>,
) {
    companion object {
        val EMPTY = Credits(cast = emptyList(), directors = emptyList(), writers = emptyList())
    }
}
