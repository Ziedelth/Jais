/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.countries

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CountryHandler(
    val name: String,
    val flag: String,
    val season: String,
    val episode: String,
    val subtitles: String,
    val dubbed: String
)
