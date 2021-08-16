/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.database

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Platform
import fr.ziedelth.jais.utils.database.components.*
import fr.ziedelth.jais.utils.tokens.DatabaseToken
import java.io.File
import java.nio.file.Files
import java.sql.Connection
import java.sql.DriverManager

object JAccess {
    private val tokenFile = File(Const.TOKENS_FOLDER, "database.json")
    private var init: Boolean
    private var databaseToken: DatabaseToken? = null
    private const val GET_PLATFORM = "SELECT * FROM jais.platforms WHERE LOWER(name) = LOWER(?) LIMIT 1"
    private const val GET_COUNTRY = "SELECT * FROM jais.countries WHERE LOWER(country) = LOWER(?) LIMIT 1"
    private const val GET_ANIME = "SELECT * FROM jais.animes WHERE countryId = ? AND LOWER(name) = LOWER(?) LIMIT 1"
    private const val GET_SEASON = "SELECT * FROM jais.seasons WHERE animeId = ? AND LOWER(name) = LOWER(?) LIMIT 1"
    private const val GET_NUMBER = "SELECT * FROM jais.numbers WHERE seasonId = ? AND LOWER(value) = LOWER(?) LIMIT 1"
    private const val GET_EPISODE =
        "SELECT * FROM jais.episodes WHERE platformId = ? AND numberId = ? AND LOWER(type) = LOWER(?) LIMIT 1"

    init {
        if (!this.tokenFile.exists()) {
            this.init = false
            JLogger.warning("Database token not exists!")
            Files.writeString(this.tokenFile.toPath(), Const.GSON.toJson(DatabaseToken()), Const.DEFAULT_CHARSET)
        } else {
            this.databaseToken = Const.GSON.fromJson(
                Files.readString(
                    this.tokenFile.toPath(),
                    Const.DEFAULT_CHARSET
                ), DatabaseToken::class.java
            )

            if (this.databaseToken?.url?.isEmpty() == true) {
                this.init = false
                JLogger.warning("Database url is empty!")
            } else {
                try {
                    getConnection()
                    this.init = true
                } catch (exception: Exception) {
                    this.init = false
                    JLogger.warning("Can not connect to database")
                }
            }
        }
    }

    fun getConnection(): Connection? = if (this.init) DriverManager.getConnection(
        this.databaseToken?.url,
        this.databaseToken?.user,
        this.databaseToken?.password
    ) else null

    private fun getJPlatform(connection: Connection?, platform: Platform): JPlatform? {
        return if (this.init && connection != null) {
            val preparedStatement = connection.prepareStatement(this.GET_PLATFORM)
            preparedStatement?.setString(1, platform.getName())
            val resultSet = preparedStatement?.executeQuery()

            if (resultSet != null && resultSet.last() && resultSet.row > 0) {
                JPlatform(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("url"),
                    resultSet.getString("image"),
                    resultSet.getString("color")
                )
            } else null
        } else null
    }

    fun insertPlatform(connection: Connection?, platform: Platform): JPlatform? {
        if (this.init && connection != null) {
            val jPlatform = getJPlatform(connection, platform)

            return if (jPlatform == null) {
                val preparedStatement =
                    connection.prepareStatement("INSERT INTO jais.platforms (name, url, image, color) VALUES (?, ?, ?, ?)")
                preparedStatement.setString(1, platform.getName())
                preparedStatement.setString(2, platform.getURL())
                preparedStatement.setString(3, platform.getImage())
                preparedStatement.setString(4, Const.toHexString(platform.getColor()))
                preparedStatement.execute()

                val resultSet = connection.prepareStatement("SELECT LAST_INSERT_ID() AS id").executeQuery()
                resultSet.first()

                JPlatform(
                    resultSet.getInt("id"),
                    platform.getName(),
                    platform.getURL(),
                    platform.getImage(),
                    Const.toHexString(platform.getColor())
                )
            } else jPlatform
        } else return null
    }

    private fun getJCountry(connection: Connection?, string: String): JCountry? {
        return if (this.init && connection != null) {
            val preparedStatement = connection.prepareStatement(this.GET_COUNTRY)
            preparedStatement.setString(1, string)
            val resultSet = preparedStatement.executeQuery()

            if (resultSet.last() && resultSet.row > 0) {
                JCountry(
                    resultSet.getInt("id"),
                    resultSet.getString("country")
                )
            } else null
        } else null
    }

    fun insertCountry(connection: Connection?, string: String): JCountry? {
        if (this.init && connection != null) {
            val jCountry = getJCountry(connection, string)

            return if (jCountry == null) {
                val preparedStatement =
                    connection.prepareStatement("INSERT INTO jais.countries (country) VALUES (?)")
                preparedStatement.setString(1, string)
                preparedStatement.execute()

                val resultSet = connection.prepareStatement("SELECT LAST_INSERT_ID() AS id").executeQuery()
                resultSet.first()

                JCountry(
                    resultSet.getInt("id"),
                    string
                )
            } else jCountry
        } else return null
    }

    private fun getJAnime(connection: Connection?, jCountry: JCountry, string: String): JAnime? {
        if (this.init && connection != null) {
            val preparedStatement = connection.prepareStatement(this.GET_ANIME)
            preparedStatement.setInt(1, jCountry.id)
            preparedStatement.setString(2, string)
            val resultSet = preparedStatement.executeQuery()

            return if (resultSet.last() && resultSet.row > 0) {
                JAnime(
                    resultSet.getInt("id"),
                    resultSet.getString("timestamp"),
                    resultSet.getInt("countryId"),
                    resultSet.getString("name"),
                    resultSet.getString("image")
                )
            } else null
        } else return null
    }

    fun insertAnime(
        connection: Connection?,
        timestamp: String,
        jCountry: JCountry,
        string: String,
        image: String?
    ): JAnime? {
        if (this.init && connection != null) {
            val jAnime = getJAnime(connection, jCountry, string)

            return if (jAnime == null) {
                val preparedStatement =
                    connection.prepareStatement("INSERT INTO jais.animes (timestamp, countryId, name, image) VALUES (?, ?, ?, ?)")
                preparedStatement.setString(1, timestamp)
                preparedStatement.setInt(2, jCountry.id)
                preparedStatement.setString(3, string)
                preparedStatement.setString(4, image)
                preparedStatement.execute()

                val resultSet = connection.prepareStatement("SELECT LAST_INSERT_ID() AS id").executeQuery()
                resultSet.first()

                JAnime(resultSet.getInt("id"), timestamp, jCountry.id, string, image)
            } else jAnime
        } else return null
    }

    private fun getJSeason(connection: Connection?, jAnime: JAnime, string: String): JSeason? {
        return if (this.init && connection != null) {
            val preparedStatement = connection.prepareStatement(this.GET_SEASON)
            preparedStatement.setInt(1, jAnime.id)
            preparedStatement.setString(2, string)
            val resultSet = preparedStatement.executeQuery()

            if (resultSet.last() && resultSet.row > 0) {
                JSeason(
                    resultSet.getInt("id"),
                    resultSet.getString("timestamp"),
                    resultSet.getInt("animeId"),
                    resultSet.getString("name")
                )
            } else null
        } else null
    }

    fun insertSeason(connection: Connection?, timestamp: String, jAnime: JAnime, string: String): JSeason? {
        if (this.init && connection != null) {
            val jSeason = getJSeason(connection, jAnime, string)

            return if (jSeason == null) {
                val preparedStatement =
                    connection.prepareStatement("INSERT INTO jais.seasons (timestamp, animeId, name) VALUES (?, ?, ?)")
                preparedStatement.setString(1, timestamp)
                preparedStatement.setInt(2, jAnime.id)
                preparedStatement.setString(3, string)
                preparedStatement.execute()

                val resultSet = connection.prepareStatement("SELECT LAST_INSERT_ID() AS id").executeQuery()
                resultSet.first()

                JSeason(
                    resultSet.getInt("id"),
                    timestamp,
                    jAnime.id,
                    string
                )
            } else jSeason
        } else return null
    }

    private fun getJNumber(connection: Connection?, jSeason: JSeason, string: String): JNumber? {
        return if (this.init && connection != null) {
            val preparedStatement = connection.prepareStatement(this.GET_NUMBER)
            preparedStatement.setInt(1, jSeason.id)
            preparedStatement.setString(2, string)
            val resultSet = preparedStatement.executeQuery()

            if (resultSet.last() && resultSet.row > 0) {
                JNumber(
                    resultSet.getInt("id"),
                    resultSet.getString("timestamp"),
                    resultSet.getInt("seasonId"),
                    resultSet.getString("value")
                )
            } else null
        } else null
    }

    fun insertNumber(connection: Connection?, timestamp: String, jSeason: JSeason, string: String): JNumber? {
        if (this.init && connection != null) {
            val jNumber = getJNumber(connection, jSeason, string)

            return if (jNumber == null) {
                val preparedStatement =
                    connection.prepareStatement("INSERT INTO jais.numbers (timestamp, seasonId, value) VALUES (?, ?, ?)")
                preparedStatement.setString(1, timestamp)
                preparedStatement.setInt(2, jSeason.id)
                preparedStatement.setString(3, string)
                preparedStatement.execute()

                val resultSet = connection.prepareStatement("SELECT LAST_INSERT_ID() AS id").executeQuery()
                resultSet.first()

                JNumber(
                    resultSet.getInt("id"),
                    timestamp,
                    jSeason.id,
                    string
                )
            } else jNumber
        } else return null
    }

    private fun getJEpisode(
        connection: Connection?,
        jPlatform: JPlatform,
        jNumber: JNumber,
        type: String
    ): JEpisode? {
        if (this.init && connection != null) {
            val preparedStatement = connection.prepareStatement(this.GET_EPISODE)
            preparedStatement.setInt(1, jPlatform.id)
            preparedStatement.setInt(2, jNumber.id)
            preparedStatement.setString(3, type)
            val resultSet = preparedStatement.executeQuery()

            return if (resultSet.last() && resultSet.row > 0) {
                JEpisode(
                    resultSet.getInt("id"),
                    resultSet.getString("timestamp"),
                    resultSet.getInt("platformId"),
                    resultSet.getInt("numberId"),
                    resultSet.getString("type"),
                    resultSet.getLong("episodeId"),
                    resultSet.getString("title") ?: null,
                    resultSet.getString("image") ?: null,
                    resultSet.getString("url") ?: null,
                    resultSet.getLong("duration")
                )
            } else null
        } else return null
    }

    fun getJEpisode(
        connection: Connection?,
        platform: String,
        country: String,
        anime: String,
        season: String,
        number: String,
        type: String
    ): JEpisode? {
        if (this.init && connection != null) {
            val preparedStatement = connection.prepareStatement(
                "SELECT * " +
                        "FROM jais.episodes, jais.numbers, jais.seasons, jais.animes, jais.countries, jais.platforms " +
                        "WHERE jais.episodes.platformId = jais.platforms.id " +
                        "AND jais.episodes.numberId = jais.numbers.id " +
                        "AND jais.numbers.seasonId = jais.seasons.id " +
                        "AND jais.seasons.animeId = jais.animes.id " +
                        "AND jais.animes.countryId = jais.countries.id " +
                        "AND jais.countries.country = ? " +
                        "AND jais.platforms.name = ? " +
                        "AND jais.animes.name = ? " +
                        "AND jais.seasons.name = ? " +
                        "AND jais.numbers.value = ? " +
                        "AND jais.episodes.type = ? " +
                        "LIMIT 1"
            )
            preparedStatement.setString(1, country)
            preparedStatement.setString(2, platform)
            preparedStatement.setString(3, anime)
            preparedStatement.setString(4, season)
            preparedStatement.setString(5, number)
            preparedStatement.setString(6, type)
            val resultSet = preparedStatement.executeQuery()

            return if (resultSet.last() && resultSet.row > 0) {
                JEpisode(
                    resultSet.getInt("id"),
                    resultSet.getString("timestamp"),
                    resultSet.getInt("platformId"),
                    resultSet.getInt("numberId"),
                    resultSet.getString("type"),
                    resultSet.getLong("episodeId"),
                    resultSet.getString("title") ?: null,
                    resultSet.getString("image") ?: null,
                    resultSet.getString("url") ?: null,
                    resultSet.getLong("duration")
                )
            } else null
        } else return null
    }

    private fun getJEpisode(connection: Connection?, jEpisode: JEpisode): JEpisode? {
        if (this.init && connection != null) {
            val preparedStatement = connection.prepareStatement(
                "SELECT * " +
                        "FROM jais.episodes " +
                        "WHERE id = ? " +
                        "LIMIT 1"
            )
            preparedStatement.setInt(1, jEpisode.id)
            val resultSet = preparedStatement.executeQuery()

            return if (resultSet.last() && resultSet.row > 0) {
                JEpisode(
                    resultSet.getInt("id"),
                    resultSet.getString("timestamp"),
                    resultSet.getInt("platformId"),
                    resultSet.getInt("numberId"),
                    resultSet.getString("type"),
                    resultSet.getLong("episodeId"),
                    resultSet.getString("title") ?: null,
                    resultSet.getString("image") ?: null,
                    resultSet.getString("url") ?: null,
                    resultSet.getLong("duration")
                )
            } else null
        } else return null
    }

    fun insertEpisode(
        connection: Connection?,
        timestamp: String,
        jPlatform: JPlatform,
        jNumber: JNumber,
        type: String,
        episodeId: Long,
        title: String?,
        image: String?,
        url: String?,
        duration: Long
    ): JEpisode? {
        if (this.init && connection != null) {
            val jEpisode = getJEpisode(connection, jPlatform, jNumber, type)

            return if (jEpisode == null) {
                val preparedStatement =
                    connection.prepareStatement("INSERT INTO jais.episodes (timestamp, platformId, numberId, type, episodeId, title, image, url, duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                preparedStatement.setString(1, timestamp)
                preparedStatement.setInt(2, jPlatform.id)
                preparedStatement.setInt(3, jNumber.id)
                preparedStatement.setString(4, type)
                preparedStatement.setLong(5, episodeId)
                preparedStatement.setString(6, title)
                preparedStatement.setString(7, image)
                preparedStatement.setString(8, url)
                preparedStatement.setLong(9, duration)
                preparedStatement.execute()

                val resultSet = connection.prepareStatement("SELECT LAST_INSERT_ID() AS id").executeQuery()
                resultSet.first()

                JEpisode(
                    resultSet.getInt("id"),
                    timestamp,
                    jPlatform.id,
                    jNumber.id,
                    type,
                    episodeId,
                    title,
                    image,
                    url,
                    duration
                )
            } else jEpisode
        } else return null
    }

    fun updateEpisode(
        connection: Connection?,
        jEpisode: JEpisode,
        episodeId: Long,
        title: String?,
        image: String?,
        url: String?,
        duration: Long
    ) {
        if (this.init && connection != null) {
            if (getJEpisode(connection, jEpisode) != null) {
                val preparedStatement =
                    connection.prepareStatement("UPDATE jais.episodes SET episodeId = ?, title = ?, image = ?, url = ?, duration = ? WHERE id = ?")
                preparedStatement.setLong(1, episodeId)
                preparedStatement.setString(2, title)
                preparedStatement.setString(3, image)
                preparedStatement.setString(4, url)
                preparedStatement.setLong(5, duration)
                preparedStatement.setInt(6, jEpisode.id)
                preparedStatement.execute()
            }
        }
    }
}