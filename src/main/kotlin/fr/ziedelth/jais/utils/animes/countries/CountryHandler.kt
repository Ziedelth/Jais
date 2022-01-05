/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.countries

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CountryHandler(
    val name: String,
    val tag: String,
    val flag: String,
    val season: String
)
