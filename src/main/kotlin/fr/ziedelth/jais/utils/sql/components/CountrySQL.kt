/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.sql.components

import fr.ziedelth.jais.utils.animes.countries.CountryHandler

data class CountrySQL(
    val id: Int,
    val name: String,
    val flag: String?,
    val season: String?,
    val episode: String?,
    val film: String?,
    val special: String?,
    val subtitles: String?,
    val dubbed: String?,
) {
    constructor(id: Int, countryHandler: CountryHandler) : this(
        id = id,
        name = countryHandler.name,
        flag = countryHandler.flag,
        season = countryHandler.season,
        episode = countryHandler.episode,
        film = countryHandler.film,
        special = countryHandler.special,
        subtitles = countryHandler.subtitles,
        dubbed = countryHandler.dubbed
    )
}