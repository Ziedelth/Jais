/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes.datas

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.animes.countries.Country

enum class EpisodeType(private val data: Array<EpisodeDataImpl>) {
    UNKNOWN(emptyArray<EpisodeDataImpl>()),
    CHAPTER(arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Chapitre"))),
    EPISODE(arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Épisode"))),
    FILM(arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Film"))),
    SPECIAL(arrayOf(EpisodeDataImpl(FranceCountry::class.java, "Spécial"))),
    ;

    fun getData(clazz: Class<out Country>?): EpisodeDataImpl? = this.data.firstOrNull { it.clazz == clazz }

    companion object {
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