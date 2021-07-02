package fr.ziedelth.ziedbot.utils.animes

enum class Country(
    val flag: String,
    val episode: String,
    val subtitles: String,
    val voice: String,
    val country: String,
    val lang: String,
    val language: String
) {
    FRANCE("🇫🇷", "Épisode", "VOSTFR", "VF", "fr", "frFR", "fr - fr"),
    UNITED_STATES("\uD83C\uDDFA\uD83C\uDDF8", "Episode", "SUB", "DUB", "us", "enUS", "en - us"),
    ;
}