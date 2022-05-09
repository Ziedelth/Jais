/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.HashUtils
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.util.*

data class News(
    val platform: Pair<PlatformHandler, Platform>,
    val country: Pair<CountryHandler, Country>,
    val releaseDate: Calendar,
    var title: String,
    val description: String,
    val url: String,
) {
    private val hash: String = HashUtils.hashString(
        "${
            this.platform.first.name.uppercase().substring(0 until 4)
        }-${this.releaseDate.timeInMillis}-${country.first.tag.uppercase()}"
    )
    val newsId: String = "${this.platform.first.name.uppercase().substring(0 until 4)}-${
        this.hash.substring(
            0,
            12
        )
    }-${country.first.tag.uppercase()}"
}