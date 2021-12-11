/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.deprecated.data

class AnimeData {
    var id: Long = -1
    var countryId: Long = -1
    var platformId: Long = -1
    lateinit var releaseDate: String
    lateinit var name: String
    lateinit var image: String
    var description: String? = null

    var country: CountryData? = null
    var platform: PlatformData? = null
    lateinit var genres: MutableList<AnimeGenreData>
    lateinit var episodes: MutableList<EpisodeData>
    lateinit var scans: MutableList<ScanData>
}
