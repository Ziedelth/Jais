/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.new.data

class ScanData {
    var id: Long = -1
    var platformId: Long = -1
    var animeId: Long = -1
    lateinit var releaseDate: String
    var number: Int = -1
    lateinit var episodeType: String
    lateinit var langType: String
    lateinit var url: String
}
