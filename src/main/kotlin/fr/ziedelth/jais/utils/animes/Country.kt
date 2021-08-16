/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

enum class Country(
    val countryName: String,
    val flag: String,
    val season: String,
    val episode: String,
    val subtitled: String,
    val dubbed: String,
    val country: String,
    val lang: String,
    val language: String
) {
    FRANCE("France", "🇫🇷", "Saison", "Épisode", "VOSTFR", "VF", "fr", "frFR", "fr - fr"),
    UNITED_STATES(
        "United States",
        "\uD83C\uDDFA\uD83C\uDDF8",
        "Season",
        "Episode",
        "SUB",
        "DUB",
        "us",
        "enUS",
        "en - us"
    ),
    ;
}