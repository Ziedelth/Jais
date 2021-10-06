/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.sql

import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.sql.components.AnimeSQL
import fr.ziedelth.jais.utils.sql.components.CountrySQL
import fr.ziedelth.jais.utils.sql.components.PlatformSQL

data class EpisodeImpl(
    val platformSQL: PlatformSQL,
    val countrySQL: CountrySQL,
    val animeSQL: AnimeSQL,
    val episode: Episode
)
