/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.animes.countries.Country

/* This class is used to define the language of the subtitles */
enum class LangType(val fr: String, val datas: Array<EpisodeDataImpl>) {
    /* It's a way to create a constant with a default value. */
    UNKNOWN("Inconnu", emptyArray<EpisodeDataImpl>()),
    SUBTITLES(
        "VOSTFR",
        arrayOf(
            EpisodeDataImpl(FranceCountry::class.java, "VOSTFR"),
            EpisodeDataImpl(FranceCountry::class.java, "VOSTF")
        )
    ),
    VOICE(
        "VF", arrayOf(
            EpisodeDataImpl(FranceCountry::class.java, "VF"),
            EpisodeDataImpl(FranceCountry::class.java, "French Dub")
        )
    ),
    ;

    /**
     * Get all the data of a specific class
     *
     * @param clazz The class of the data you want to get.
     */
    fun getDatas(clazz: Class<out Country>?): List<EpisodeDataImpl> = this.datas.filter { it.clazz == clazz }

    /**
     * Get the data for the given class
     *
     * @param clazz The class of the data you want to get.
     */
    fun getData(clazz: Class<out Country>?): EpisodeDataImpl? = this.datas.firstOrNull { it.clazz == clazz }

    companion object {
        /**
         * Given a string, return the LangType that matches the string
         *
         * @param string The string to check.
         * @return The LangType enum value that matches the string.
         */
        fun getLangType(string: String?): LangType {
            for (type in values()) {
                for (data in type.datas) {
                    if (data.data.equals(string, true)) {
                        return type
                    }
                }
            }

            return UNKNOWN
        }

    }
}