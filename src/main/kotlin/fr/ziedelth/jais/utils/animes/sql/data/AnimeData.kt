/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.data

class AnimeData {
    var id: Long = -1
    lateinit var releaseDate: String
    lateinit var name: String
    var image: String? = null
    var description: String? = null

    lateinit var codes: MutableList<AnimeCodeData>
    lateinit var genres: MutableList<AnimeGenreData>
    lateinit var episodes: MutableList<EpisodeData>
    lateinit var scans: MutableList<ScanData>
}
