package fr.ziedelth.ziedbot.utils.animes

enum class Country(
    val countryName: String,
    val flag: String,
    val episode: String,
    val subtitled: String,
    val dubbed: String,
    val country: String,
    val lang: String,
    val language: String
) {
    FRANCE("France", "🇫🇷", "Épisode", "VOSTFR", "VF", "fr", "frFR", "fr - fr"),
    UNITED_STATES("United States", "\uD83C\uDDFA\uD83C\uDDF8", "Episode", "SUB", "DUB", "us", "enUS", "en - us"),
    ;
}