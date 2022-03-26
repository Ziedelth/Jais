/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import java.util.*

/**
 * A news object is a data class that contains a platform, country, release date, title, description, and url.
 */
data class News(
    val platform: PlatformImpl,
    val country: CountryImpl,
    val releaseDate: Calendar,
    var title: String,
    val description: String,
    val url: String,
) {
    /**
     * "If the two objects are the same, return true. If they're not the same class, return false. If they're not the same
     * platform, return false. If they're not the same country, return false. If they're not the same release date, return
     * false. If they're not the same title, return false. If they're not the same description, return false. If they're
     * not the same url, return false. Otherwise, return true."
     *
     * The function is pretty straightforward. It's just a bunch of if statements that check if the two objects are the
     * same. If they are, return true. If they're not, return false
     *
     * @param other Any?
     * @return Nothing.
     */
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

    /**
     * "Compute a hash code for this object."
     *
     * The `override` keyword is used to indicate that we are overriding a method from the superclass
     *
     * @return Nothing.
     */
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