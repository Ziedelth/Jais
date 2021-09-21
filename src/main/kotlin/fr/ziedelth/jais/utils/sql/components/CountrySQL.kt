/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.sql.components

data class CountrySQL(
    val id: Int,
    val name: String,
    val flag: String?,
    val season: String?,
    val episode: String?,
    val subtitles: String?,
    val dubbed: String?,
)