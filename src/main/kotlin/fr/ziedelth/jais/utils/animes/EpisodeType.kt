/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.animes.countries.Country
import kotlin.reflect.KClass

enum class EpisodeType(val fr: String, private val data: Array<Pair<Class<out Country>, String>>) {
    CHAPTER("Chapitre", arrayOf(FranceCountry::class.java to "Chapitre")),
    EPISODE("Épisode", arrayOf(FranceCountry::class.java to "Épisode")),
    FILM("Film", arrayOf(FranceCountry::class.java to "Film")),
    SPECIAL("Spécial", arrayOf(FranceCountry::class.java to "Spécial")),
    ;

    fun getData(clazz: Class<out Country>?) = this.data.firstOrNull { it.first == clazz }
}