/*
 * Copyright (c) 2021. Ziedelth
 */

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform
import fr.ziedelth.jais.platforms.CrunchyrollPlatform
import fr.ziedelth.jais.platforms.ScantradPlatform
import fr.ziedelth.jais.platforms.WakanimPlatform
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import fr.ziedelth.jais.utils.animes.sql.JMapper

/*
 * Copyright (c) 2021. Ziedelth
 */

fun main() {
    val countries = arrayOf(addCountry(FranceCountry::class.java)).filterNotNull()
    val platforms = arrayOf(
        addPlatform(AnimeDigitalNetworkPlatform::class.java),
        addPlatform(CrunchyrollPlatform::class.java),
        addPlatform(ScantradPlatform::class.java),
        addPlatform(WakanimPlatform::class.java)
    ).filterNotNull()

    val connection = JMapper.getConnection()
    val jConnection = JMapper.getConnection()
    jConnection?.autoCommit = false

    Impl.tryCatch({
        countries.forEach { JMapper.insertCountry(jConnection, it.countryHandler.name, it.countryHandler.flag) }
        platforms.forEach {
            JMapper.insertPlatform(
                jConnection,
                it.platformHandler.name,
                it.platformHandler.url,
                it.platformHandler.image,
                it.platformHandler.color
            )
        }
        Genre.values().forEach { JMapper.insertGenre(jConnection, it.name) }

        JMapper.getAnimes(connection).forEach {
            if (it.country == null || it.platform == null) return@forEach
            val country = JMapper.getCountry(jConnection, it.country!!.name)
            val platform = JMapper.getPlatform(jConnection, it.platform!!.name)

            if (country != null && platform != null) {
                val anime = JMapper.insertAnime(
                    jConnection,
                    country.id,
                    it.releaseDate,
                    it.name,
                    it.image,
                    it.description,
                    false
                )

                if (anime != null) {
                    it.genres.forEach { animeGenreData ->
                        val genre = JMapper.getGenre(jConnection, animeGenreData.genre)
                        if (genre != null) JMapper.insertAnimeGenre(jConnection, anime.id, genre.id)
                    }

                    it.episodes.sortedBy { data -> ISO8601.fromUTCDate(data.releaseDate) }.forEach { episodeData ->
                        JMapper.insertEpisode(
                            jConnection,
                            platform.id,
                            anime.id,
                            episodeData.releaseDate,
                            episodeData.season,
                            episodeData.number,
                            episodeData.episodeType,
                            episodeData.langType,
                            episodeData.episodeId,
                            episodeData.title,
                            episodeData.url,
                            episodeData.image,
                            episodeData.duration,
                            false
                        )
                    }
                    it.scans.sortedBy { data -> ISO8601.fromUTCDate(data.releaseDate) }.forEach { scanData ->
                        JMapper.insertScan(
                            jConnection,
                            platform.id,
                            anime.id,
                            scanData.releaseDate,
                            scanData.number,
                            scanData.episodeType,
                            scanData.langType,
                            scanData.url
                        )
                    }
                }
            }
        }

        jConnection?.commit()
    }, {
        jConnection?.rollback()
    })
}

private fun addCountry(country: Class<out Country>): CountryImpl? {
    return if (country.isAnnotationPresent(CountryHandler::class.java)) {
        CountryImpl(
            countryHandler = country.getAnnotation(CountryHandler::class.java),
            country = country.getConstructor().newInstance()
        )
    } else null
}

private fun addPlatform(platform: Class<out Platform>): PlatformImpl? {
    return if (platform.isAnnotationPresent(PlatformHandler::class.java)) {
        PlatformImpl(
            platformHandler = platform.getAnnotation(PlatformHandler::class.java),
            platform = platform.getConstructor().newInstance()
        )
    } else null
}