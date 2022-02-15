/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as News

        if (platform != other.platform) return false
        if (country != other.country) return false
        if (releaseDate != other.releaseDate) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = platform.hashCode()
        result = 31 * result + country.hashCode()
        result = 31 * result + releaseDate.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }
}