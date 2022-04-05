/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.HashUtils
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import java.util.*

data class News(
    val platform: PlatformImpl,
    val country: CountryImpl,
    val releaseDate: Calendar,
    var title: String,
    val description: String,
    val url: String,
) {
    private val hash: String = HashUtils.sha512(
        "${
            this.platform.platformHandler.name.uppercase().substring(0 until 4)
        }-${this.releaseDate.timeInMillis}-${country.countryHandler.tag.uppercase()}"
    )
    val newsId: String = "${this.platform.platformHandler.name.uppercase().substring(0 until 4)}-${
        this.hash.substring(
            0,
            12
        )
    }-${country.countryHandler.tag.uppercase()}"
}