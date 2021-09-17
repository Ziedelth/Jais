/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.platforms

import fr.ziedelth.jais.utils.animes.countries.Country
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PlatformHandler(
    val name: String,
    val url: String,
    val image: String,
    val color: Int,
    val countries: Array<KClass<out Country>>
)
