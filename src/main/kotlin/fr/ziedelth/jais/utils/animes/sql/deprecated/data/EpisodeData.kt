/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.deprecated.data

class EpisodeData {
    var id: Long = -1
    var animeId: Long = -1
    lateinit var releaseDate: String
    var season: Int = -1
    var number: Int = -1
    lateinit var episodeType: String
    lateinit var langType: String
    lateinit var episodeId: String
    var title: String? = null
    lateinit var url: String
    lateinit var image: String
    var duration: Long = 0
}
