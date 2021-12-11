/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.new.data

class AnimeData {
    var id: Long = -1
    var countryId: Long = -1
    lateinit var releaseDate: String
    lateinit var code: String
    lateinit var name: String
    var image: String? = null
    var description: String? = null

    lateinit var episodes: MutableList<EpisodeData>
}
