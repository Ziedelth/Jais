/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.data

class ScanData {
    var id: Long = -1
    var platformId: Long = -1
    var animeId: Long = -1
    var idEpisodeType: Long = -1
    var idLangType: Long = -1
    lateinit var releaseDate: String
    lateinit var number: String
    lateinit var url: String
}
