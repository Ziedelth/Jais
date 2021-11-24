/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.scans.platforms

import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.AnimeGenre
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import fr.ziedelth.jais.utils.animes.scans.Scan
import org.jsoup.Jsoup

data class ScantradScan(
    val title: String?,
    val link: String?,
    val description: String?,
    val pubDate: String?,
    var genres: Array<AnimeGenre>? = null,
    var animeDescription: String? = null,
) {
    var platformImpl: PlatformImpl? = null
    var countryImpl: CountryImpl? = null

    fun isValid(): Boolean {
        return this.platformImpl != null &&
                this.countryImpl != null &&
                ISO8601.fromUTCDate(ISO8601.toUTCDate(ISO8601.fromCalendar(ISO8601.toCalendar2(this.pubDate)))) != null &&
                !this.title.isNullOrBlank() &&
                !this.link.isNullOrBlank()
    }

    fun toScan(): Scan? {
        val titleSplitter = this.title?.split("Scan - ")?.get(1)?.split(" ")
        val descriptionDocument = Jsoup.parse(this.description ?: "")

        return if (this.isValid()) Scan(
            platform = this.platformImpl!!,
            country = this.countryImpl!!,
            releaseDate = ISO8601.fromUTCDate(ISO8601.toUTCDate(ISO8601.fromCalendar(ISO8601.toCalendar2(this.pubDate))))!!,
            anime = titleSplitter!!.subList(0, titleSplitter.size - 2).joinToString(" "),
            animeImage = descriptionDocument.getElementsByTag("img").attr("src"),
            animeGenres = this.genres ?: emptyArray(),
            animeDescription = this.animeDescription,
            number = titleSplitter.last().toLong(),
            url = this.link!!.replace("http://", "https://")
        ) else null
    }
}
