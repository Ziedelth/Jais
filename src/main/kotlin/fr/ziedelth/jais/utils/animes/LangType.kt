/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.animes.countries.Country

enum class LangType(val fr: String, val datas: Array<Pair<Class<out Country>, String>>) {
    UNKNOWN("Inconnu", emptyArray()),
    SUBTITLES(
        "VOSTFR",
        arrayOf(FranceCountry::class.java to "VOSTFR", FranceCountry::class.java to "VOSTF")
    ),
    VOICE(
        "VF", arrayOf(FranceCountry::class.java to "VF", FranceCountry::class.java to "French Dub")
    ),
    ;

    fun getDatas(clazz: Class<out Country>?) = this.datas.filter { it.first == clazz }
    fun getData(clazz: Class<out Country>?) = this.datas.firstOrNull { it.first == clazz }

    companion object {
        fun getLangType(string: String?): LangType {
            for (type in values()) {
                for (data in type.datas) {
                    if (data.second.equals(string, true)) {
                        return type
                    }
                }
            }

            return UNKNOWN
        }

    }
}