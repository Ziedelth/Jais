/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.sql.components

import fr.ziedelth.jais.utils.animes.episodes.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.LangType

data class EpisodeSQL(
    val id: Int,
    val platformId: Int,
    val countryId: Int,
    val animeId: Int,
    val releaseDate: String,
    val season: Int,
    val number: Int,
    val episodeType: EpisodeType,
    val langType: LangType,
    val eId: String,
    val title: String?,
    val url: String?,
    val image: String?,
    val duration: Long,
)