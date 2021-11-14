/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.data

class AnimeData {
    var id: Long = -1
    var countryId: Long = -1
    lateinit var releaseDate: String
    lateinit var name: String
    lateinit var image: String
    var description: String? = null

    var country: CountryData? = null
    lateinit var genres: MutableList<AnimeGenreData>
    lateinit var episodes: MutableList<EpisodeData>
}
