/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.sql.components

import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.LangType

data class EpisodeSQL(
    val id: Int,
    val platformId: Int,
    val countryId: Int,
    val animeId: Int,
    val releaseDate: String,
    val season: Long,
    val number: Long,
    val episodeType: EpisodeType,
    val langType: LangType,
    val eId: String,
    val title: String?,
    val url: String?,
    val image: String?,
    val duration: Long,
) {
    constructor(id: Int, platformSQL: PlatformSQL, countrySQL: CountrySQL, animeSQL: AnimeSQL, episode: Episode) : this(
        id = id,
        platformId = platformSQL.id,
        countryId = countrySQL.id,
        animeId = animeSQL.id,
        releaseDate = episode.releaseDate,
        season = episode.season,
        number = episode.number,
        episodeType = episode.episodeType,
        langType = episode.langType,
        eId = episode.eId,
        title = episode.title,
        url = episode.url,
        image = episode.image,
        duration = episode.duration
    )
}