/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.animes.countries.Country

/* This class is used to determine the type of episode based on the country and the episode name */
enum class EpisodeType(val fr: String, private val data: Array<EpisodeDataImpl>) {
    UNKNOWN("Inconnu", emptyArray<EpisodeDataImpl>()),
    CHAPTER("Chapitre", arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Chapitre"))),
    EPISODE("Épisode", arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Épisode"))),
    FILM("Film", arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Film"))),
    SPECIAL("Spécial", arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Spécial"))),
    ;

    fun getData(clazz: Class<out Country>?): EpisodeDataImpl? = this.data.firstOrNull { it.clazz == clazz }

    companion object {
        /**
         * Given a string, return the corresponding EpisodeType
         *
         * @param string The string to check against.
         * @return The type of episode.
         */
        fun getEpisodeType(string: String?): EpisodeType {
            for (type in values()) {
                for (data in type.data) {
                    if (data.data.equals(string, true)) {
                        return type
                    }
                }
            }

            return UNKNOWN
        }

        /**
         * Given a class, return the corresponding episode type
         *
         * @param clazz The class of the country.
         * @return The type of episode that the given class is.
         */
        fun getEpisodeType(clazz: Class<out Country>?): EpisodeType {
            for (type in values()) {
                for (data in type.data) {
                    if (data.clazz == clazz) {
                        return type
                    }
                }
            }

            return UNKNOWN
        }
    }
}