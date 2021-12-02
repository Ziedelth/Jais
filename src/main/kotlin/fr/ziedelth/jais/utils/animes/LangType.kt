/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.animes.countries.Country

enum class LangType(private val data: Array<EpisodeDataImpl>) {
    UNKNOWN(emptyArray<EpisodeDataImpl>()),
    SUBTITLES(
        arrayOf(
            EpisodeDataImpl(FranceCountry::class.java, "VOSTFR"),
            EpisodeDataImpl(FranceCountry::class.java, "VOSTF")
        )
    ),
    VOICE(arrayOf(EpisodeDataImpl(FranceCountry::class.java, "VF"))),
    ;

    fun getData(clazz: Class<out Country>?): EpisodeDataImpl? = this.data.firstOrNull { it.clazz == clazz }

    companion object {
        fun getLangType(string: String?): LangType {
            for (type in values()) {
                for (data in type.data) {
                    if (data.data.equals(string, true)) {
                        return type
                    }
                }
            }

            return UNKNOWN
        }

        fun getLangType(clazz: Class<out Country>?): LangType {
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