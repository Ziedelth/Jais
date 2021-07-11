package fr.ziedelth.ziedbot.utils

import fr.ziedelth.ziedbot.utils.animes.Country

data class Channel(
    var anime: Boolean = true,
    var news: Boolean = true,
    var countries: MutableList<Country> = mutableListOf()
)
