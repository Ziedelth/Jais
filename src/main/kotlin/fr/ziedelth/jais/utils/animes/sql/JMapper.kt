/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql

import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.Scan
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import fr.ziedelth.jais.utils.animes.sql.data.*
import fr.ziedelth.jais.utils.animes.sql.mappers.*
import java.sql.Connection
import java.sql.DriverManager
import java.util.Calendar

object JMapper {
    private val countryMapper = CountryMapper()
    private val platformMapper = PlatformMapper()
    private val genreMapper = GenreMapper()
    private val episodeTypeMapper = EpisodeTypeMapper()
    private val langTypeMapper = LangTypeMapper()
    private val animeGenreMapper = AnimeGenreMapper()

    val animeMapper = AnimeMapper()
    val episodeMapper = EpisodeMapper()
    val scanMapper = ScanMapper()
    val opsEndsMapper = OpsEndsMapper()

    /**
     * It returns a connection to the database if the configuration is valid
     *
     * @return Nothing.
     */
    fun getConnection(): Connection? {
        val configuration = Configuration.load() ?: return null
        return DriverManager.getConnection(configuration.url, configuration.user, configuration.password)
    }

    /**
     * It returns a connection to the database
     */
    fun getDebugConnection(): Connection? =
        DriverManager.getConnection("jdbc:mariadb://localhost:3306/jais", "root", "root")

    private fun initInsert(connection: Connection?, platform: PlatformImpl, country: CountryImpl, releaseDate: Calendar, anime: String, animeImage: String?, animeDescription: String?, genres: Array<Genre>): Pair<PlatformData?, AnimeData?> {
        val platformData = this.platformMapper.insert(connection, platform.platformHandler)
        val countryData = this.countryMapper.insert(connection, country.countryHandler)

        val animeData = this.animeMapper.insert(
            connection,
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

    /**
     * Insert an episode into the database
     *
     * @param connection The connection to the database.
     * @param episode The Episode object that we want to insert.
     * @return Nothing.
     */
    fun insertEpisode(connection: Connection?, episode: Episode): EpisodeData? {
        val (platformData, animeData) = this.initInsert(
            connection,
            episode.platform,
            episode.country,
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

    /**
     * Insert the scan into the database
     *
     * @param connection The connection to the database.
     * @param scan Scan
     * @return Nothing.
     */
    fun insertScan(connection: Connection?, scan: Scan): ScanData? {
        val (platformData, animeData) = this.initInsert(
            connection,
            scan.platform,
            scan.country,
            scan.releaseDate,
            scan.anime,
            scan.animeImage,
            scan.animeDescription,
            scan.animeGenres
        )

        return this.scanMapper.insert(
            connection,
            platformData?.id,
            animeData?.id,
            this.episodeTypeMapper.insert(connection, scan.episodeType)?.id,
            this.langTypeMapper.insert(connection, scan.langType)?.id,
            ISO8601.toUTCDate(ISO8601.fromCalendar(scan.releaseDate)),
            scan.number,
            scan.url
        )
    }
}