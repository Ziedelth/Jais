/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.animes.countries.Country

/* This class is used to determine the type of episode based on the country and the episode name */
enum class EpisodeType(val fr: String, private val data: Array<EpisodeDataImpl>) {
    CHAPTER("Chapitre", arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Chapitre"))),
    EPISODE("Épisode", arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Épisode"))),
    FILM("Film", arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Film"))),
    SPECIAL("Spécial", arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Spécial"))),
    ;

    fun getData(clazz: Class<out Country>?): EpisodeDataImpl? = this.data.firstOrNull { it.clazz == clazz }
}