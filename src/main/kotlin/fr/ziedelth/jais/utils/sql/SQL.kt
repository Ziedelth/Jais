/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.sql

import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.LangType
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.sql.components.AnimeSQL
import fr.ziedelth.jais.utils.sql.components.CountrySQL
import fr.ziedelth.jais.utils.sql.components.EpisodeSQL
import fr.ziedelth.jais.utils.sql.components.PlatformSQL
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

object SQL {
    fun getConnection(): Connection? {
        return DriverManager.getConnection("jdbc:mariadb://localhost:3306/ziedelth", "root", "")
    }

    /*
    PLATFORMS
     */

    private fun getPlatformIsInDatabase(connection: Connection?, platformHandler: PlatformHandler): PlatformSQL? {
        val ps = connection?.prepareStatement("SELECT id FROM platforms WHERE LOWER(name) = ? LIMIT 1;")
        ps?.setString(1, platformHandler.name.lowercase())
        val rs = ps?.executeQuery()

        if (rs?.next() == true) return PlatformSQL(
            id = rs.getInt("id"),
            platformHandler = platformHandler
        )
        return null
    }

    private fun insertPlatformInDatabase(connection: Connection?, platformHandler: PlatformHandler): Int {
        val ps = connection?.prepareStatement(
            "INSERT INTO platforms (name, url, image, color) VALUES (?, ?, ?, ?);",
            Statement.RETURN_GENERATED_KEYS
        )
        ps?.setString(1, platformHandler.name)
        ps?.setString(2, platformHandler.url)
        ps?.setString(3, platformHandler.image)
        ps?.setString(4, platformHandler.color.toString(16))
        val affectedRows = ps?.executeUpdate() == 1

        return if (affectedRows) {
            val rs = ps?.generatedKeys
            if (rs?.next() == true) rs.getInt("id")
            else 0
        } else 0
    }

    private fun updatePlatformInDatabase(connection: Connection?, platformHandler: PlatformHandler): Boolean {
        val ps =
            connection?.prepareStatement("UPDATE platforms SET url = ?, image = ?, color = ? WHERE LOWER(name) = ? LIMIT 1;")
        ps?.setString(1, platformHandler.url)
        ps?.setString(2, platformHandler.image)
        ps?.setString(3, platformHandler.color.toString(16))
        ps?.setString(4, platformHandler.name.lowercase())
        return ps?.executeUpdate() == 1
    }

    fun insertOrUpdatePlatform(connection: Connection?, platformHandler: PlatformHandler): PlatformSQL? {
        val platformSQL = getPlatformIsInDatabase(connection, platformHandler)

        if (platformSQL != null) {
            if (platformHandler.url != platformSQL.url || platformHandler.image != platformSQL.image || platformHandler.color.toString(
                    16
                ) != platformSQL.color
            ) {
                if (!updatePlatformInDatabase(
                        connection,
                        platformHandler
                    )
                ) JLogger.warning("Failed to update platform in database")
                else return getPlatformIsInDatabase(connection, platformHandler)
            }
        } else {
            val id = insertPlatformInDatabase(connection, platformHandler)
            if (id == 0) JLogger.warning("Failed to insert platform in database")
            else return PlatformSQL(id = id, platformHandler = platformHandler)
        }

        return platformSQL
    }

    /*
    COUNTRIES
     */

    private fun getCountryIsInDatabase(connection: Connection?, countryHandler: CountryHandler): CountrySQL? {
        val ps = connection?.prepareStatement("SELECT id FROM countries WHERE LOWER(name) = ? LIMIT 1;")
        ps?.setString(1, countryHandler.name.lowercase())
        val rs = ps?.executeQuery()

        if (rs?.next() == true) return CountrySQL(id = rs.getInt("id"), countryHandler = countryHandler)
        return null
    }

    private fun insertCountryInDatabase(connection: Connection?, countryHandler: CountryHandler): Int {
        val ps =
            connection?.prepareStatement("INSERT INTO countries (name, flag, season, episode, film, special, subtitles, dubbed) VALUES (?, ?, ?, ?, ?, ?, ?, ?);")
        ps?.setString(1, countryHandler.name)
        ps?.setString(2, countryHandler.flag)
        ps?.setString(3, countryHandler.season)
        ps?.setString(4, countryHandler.episode)
        ps?.setString(5, countryHandler.film)
        ps?.setString(6, countryHandler.special)
        ps?.setString(7, countryHandler.subtitles)
        ps?.setString(8, countryHandler.dubbed)
        val affectedRows = ps?.executeUpdate() == 1

        return if (affectedRows) {
            val rs = ps?.generatedKeys
            if (rs?.next() == true) rs.getInt("id")
            else 0
        } else 0
    }

    private fun updateCountryInDatabase(connection: Connection?, countryHandler: CountryHandler): Boolean {
        val ps =
            connection?.prepareStatement("UPDATE countries SET flag = ?, season = ?, episode = ?, film = ?, special = ?, subtitles = ?, dubbed = ? WHERE LOWER(name) = ? LIMIT 1;")
        ps?.setString(1, countryHandler.flag)
        ps?.setString(2, countryHandler.season)
        ps?.setString(3, countryHandler.episode)
        ps?.setString(4, countryHandler.film)
        ps?.setString(5, countryHandler.special)
        ps?.setString(6, countryHandler.subtitles)
        ps?.setString(7, countryHandler.dubbed)
        ps?.setString(8, countryHandler.name.lowercase())
        return ps?.executeUpdate() == 1
    }

    fun insertOrUpdateCountry(connection: Connection?, countryHandler: CountryHandler): CountrySQL? {
        val countrySQL = getCountryIsInDatabase(connection, countryHandler)

        if (countrySQL != null) {
            if (countryHandler.flag != countrySQL.flag || countryHandler.season != countrySQL.season || countryHandler.episode != countrySQL.episode || countryHandler.film != countrySQL.film || countryHandler.special != countrySQL.special || countryHandler.subtitles != countrySQL.subtitles || countryHandler.dubbed != countrySQL.dubbed) {
                if (!updateCountryInDatabase(
                        connection,
                        countryHandler
                    )
                ) JLogger.warning("Failed to update country in database")
                else return getCountryIsInDatabase(connection, countryHandler)
            }
        } else {
            val id = insertCountryInDatabase(connection, countryHandler)
            if (id == 0) JLogger.warning("Failed to insert country in database")
            else return CountrySQL(id = id, countryHandler = countryHandler)
        }

        return countrySQL
    }

    /*
    ANIMES
     */

    private fun getAnimeIsInDatabase(connection: Connection?, name: String): AnimeSQL? {
        val ps = connection?.prepareStatement("SELECT * FROM animes WHERE LOWER(name) = ? LIMIT 1;")
        ps?.setString(1, name.lowercase())
        val rs = ps?.executeQuery()

        if (rs?.next() == true) return AnimeSQL(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            releaseDate = rs.getString("releaseDate"),
            image = rs.getString("image"),
        )
        return null
    }

    private fun insertAnimeInDatabase(connection: Connection?, name: String, releaseDate: String): Int {
        val ps = connection?.prepareStatement(
            "INSERT INTO animes (name, releaseDate, image) VALUES (?, ?, NULL);",
            Statement.RETURN_GENERATED_KEYS
        )
        ps?.setString(1, name)
        ps?.setString(2, releaseDate)
        val affectedRows = ps?.executeUpdate() == 1

        return if (affectedRows) {
            val rs = ps?.generatedKeys
            if (rs?.next() == true) rs.getInt("id")
            else 0
        } else 0
    }

    fun insertOrUpdateAnime(connection: Connection?, name: String, releaseDate: String): AnimeSQL? {
        val animeSQL = getAnimeIsInDatabase(connection, name)

        if (animeSQL == null) {
            val id = insertAnimeInDatabase(connection, name, releaseDate)
            if (id == 0) JLogger.warning("Failed to insert anime in database")
            else return AnimeSQL(id = id, name = name, releaseDate = releaseDate, image = null)
        }

        return animeSQL
    }

    /*
    EPISODES
     */

    fun getEpisodeIsInDatabase(connection: Connection?, episode: Episode): EpisodeSQL? {
        val ps = connection?.prepareStatement("SELECT * FROM episodes WHERE LOWER(eId) = ? LIMIT 1;")
        ps?.setString(1, episode.eId.lowercase())
        val rs = ps?.executeQuery()

        if (rs?.next() == true) return EpisodeSQL(
            id = rs.getInt("id"),
            platformId = rs.getInt("platformId"),
            countryId = rs.getInt("countryId"),
            animeId = rs.getInt("animeId"),
            releaseDate = rs.getString("releaseDate"),
            season = rs.getLong("season"),
            number = rs.getLong("number"),
            episodeType = EpisodeType.valueOf(rs.getString("episodeType")),
            langType = LangType.valueOf(rs.getString("langType")),
            eId = rs.getString("eId"),
            title = rs.getString("title"),
            url = rs.getString("url"),
            image = rs.getString("image"),
            duration = rs.getLong("duration"),
        )
        return null
    }

    fun insertEpisodeInDatabase(
        connection: Connection?,
        platformSQL: PlatformSQL,
        countrySQL: CountrySQL,
        animeSQL: AnimeSQL,
        episode: Episode
    ): Int {
        val ps = connection?.prepareStatement(
            "INSERT INTO episodes (platformId, countryId, animeId, releaseDate, season, number, episodeType, langType, eId, title, url, image, duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
            Statement.RETURN_GENERATED_KEYS
        )
        ps?.setInt(1, platformSQL.id)
        ps?.setInt(2, countrySQL.id)
        ps?.setInt(3, animeSQL.id)
        ps?.setString(4, episode.releaseDate)
        ps?.setLong(5, episode.season)
        ps?.setLong(6, episode.number)
        ps?.setString(7, episode.episodeType.name)
        ps?.setString(8, episode.langType.name)
        ps?.setString(9, episode.eId)
        ps?.setString(10, episode.title)
        ps?.setString(11, episode.url)
        ps?.setString(12, episode.image)
        ps?.setLong(13, episode.duration)
        val affectedRows = ps?.executeUpdate() == 1

        return if (affectedRows) {
            val rs = ps?.generatedKeys
            if (rs?.next() == true) rs.getInt("id")
            else 0
        } else 0
    }

    fun updateEpisodeInDatabase(connection: Connection?, episode: Episode): Boolean {
        val ps =
            connection?.prepareStatement("UPDATE episodes SET title = ?, url = ?, image = ?, duration = ? WHERE LOWER(eId) = ? LIMIT 1;")
        ps?.setString(1, episode.title)
        ps?.setString(2, episode.url)
        ps?.setString(3, episode.image)
        ps?.setLong(4, episode.duration)
        ps?.setString(5, episode.eId.lowercase())
        return ps?.executeUpdate() == 1
    }

    fun lastSpecialEpisode(
        connection: Connection?,
        platformSQL: PlatformSQL,
        countrySQL: CountrySQL,
        animeSQL: AnimeSQL,
        season: Long
    ): Long {
        val ps =
            connection?.prepareStatement("SELECT number FROM episodes WHERE platformId = ? AND countryId = ? AND animeId = ? AND season = ? AND episodeType = ? ORDER BY releaseDate DESC LIMIT 1;")
        ps?.setInt(1, platformSQL.id)
        ps?.setInt(2, countrySQL.id)
        ps?.setInt(3, animeSQL.id)
        ps?.setLong(4, season)
        ps?.setString(5, EpisodeType.SPECIAL.name)
        val rs = ps?.executeQuery()

        if (rs?.next() == true) return rs.getLong("number")
        return 0
    }
}