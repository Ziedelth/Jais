package fr.ziedelth.jais.utils

import fr.ziedelth.jais.utils.animes.Country

data class Channel(
    var anime: Boolean = true,
    var news: Boolean = true,
    var countries: MutableList<Country> = mutableListOf()
)
