/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql

import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.sql.data.AnimeData
import fr.ziedelth.jais.utils.animes.sql.data.EpisodeData
import fr.ziedelth.jais.utils.animes.sql.data.PlatformData
import fr.ziedelth.jais.utils.animes.sql.mappers.*
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

object JMapper {
    private val countryMapper = CountryMapper()
    private val platformMapper = PlatformMapper()
    private val genreMapper = GenreMapper()
    private val episodeTypeMapper = EpisodeTypeMapper()
    private val langTypeMapper = LangTypeMapper()
    private val animeGenreMapper = AnimeGenreMapper()
    private val animeCodeMapper = AnimeCodeMapper()

    val animeMapper = AnimeMapper()
    val episodeMapper = EpisodeMapper()

    fun getConnection(): Connection? {
        val configuration = Configuration.load() ?: return null
        return DriverManager.getConnection(configuration.url, configuration.user, configuration.password)
    }

    private fun initInsert(
        connection: Connection?,
        platform: PlatformHandler,
        country: CountryHandler,
        releaseDate: Calendar,
        anime: String,
        animeImage: String?,
        animeDescription: String?,
        genres: Array<Genre>
    ): Pair<PlatformData?, AnimeData?> {
        val platformData = this.platformMapper.insert(connection, platform)
        val countryData = this.countryMapper.insert(connection, country)

        val animeData = this.animeMapper.insert(
            connection,
            this.animeCodeMapper,
            countryData?.id,
            ISO8601.toUTCDate(ISO8601.fromCalendar(releaseDate)),
            anime,
            animeImage,
            animeDescription
        )

        if (animeData?.genres?.isEmpty() == true) {
            genres.forEach {
                val genre = this.genreMapper.insert(connection, it)
                if (genre != null)
                    this.animeGenreMapper.insert(
                        connection,
                        animeData.id,
                        genre.id
                    )
            }
        }

        return Pair(platformData, animeData)
    }

    fun insertEpisode(connection: Connection?, episode: Episode): EpisodeData? {
        val (platformData, animeData) = this.initInsert(
            connection,
            episode.platform.first,
            episode.country.first,
            episode.releaseDate,
            episode.anime,
            episode.animeImage,
            episode.animeDescription,
            episode.animeGenres
        )

        return this.episodeMapper.insert(
            connection,
            animeMapper,
            platformData?.id,
            animeData?.id,
            this.episodeTypeMapper.insert(connection, episode.episodeType)?.id,
            this.langTypeMapper.insert(connection, episode.langType)?.id,
            ISO8601.toUTCDate(ISO8601.fromCalendar(episode.releaseDate)),
            episode.season.toInt(),
            episode.number.toInt(),
            episode.episodeId,
            episode.title,
            episode.url,
            episode.image,
            episode.duration
        )
    }
}