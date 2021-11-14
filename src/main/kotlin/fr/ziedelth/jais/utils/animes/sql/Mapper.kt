/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.episodes.datas.EpisodeType
import fr.ziedelth.jais.utils.animes.sql.data.*
import fr.ziedelth.jais.utils.animes.sql.handlers.AnimeGenreHandler
import fr.ziedelth.jais.utils.animes.sql.handlers.AnimeHandler
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanListHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.io.File
import java.net.URL
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import javax.imageio.ImageIO


object Mapper {
    fun getConnection(): Connection? = DriverManager.getConnection("jdbc:mariadb://localhost:3306/ziedelth", "root", "")

    fun getCountries(connection: Connection?): MutableList<CountryData> {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries", blh)
    }

    private fun getCountry(connection: Connection?, id: Long): CountryData? {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries WHERE id = ?", blh, id).firstOrNull()
    }

    private fun getCountry(connection: Connection?, name: String): CountryData? {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries WHERE name = ?", blh, name).firstOrNull()
    }

    fun insertCountry(connection: Connection?, name: String, flag: String): CountryData? {
        val country = getCountry(connection, name)

        return if (country != null) country
        else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO countries (id, name, flag) VALUES (NULL, ?, ?)"
            val newId: Long = runner.insert(connection, query, sh, name, flag).toLong()
            getCountry(connection, newId)
        }
    }

    fun getPlatforms(connection: Connection?): MutableList<PlatformData> {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms", blh)
    }

    private fun getPlatform(connection: Connection?, id: Long): PlatformData? {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms WHERE id = ?", blh, id).firstOrNull()
    }

    private fun getPlatform(connection: Connection?, name: String): PlatformData? {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms WHERE name = ?", blh, name).firstOrNull()
    }

    fun insertPlatform(connection: Connection?, name: String, url: String, image: String): PlatformData? {
        val platform = getPlatform(connection, name)

        return if (platform != null) platform
        else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO platforms (id, name, url, image) VALUES (NULL, ?, ?, ?)"
            val newId: Long = runner.insert(connection, query, sh, name, url, image).toLong()
            getPlatform(connection, newId)
        }
    }

    fun getAnimes(connection: Connection?): MutableList<AnimeData> {
        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes", ah)
    }

    private fun getAnime(connection: Connection?, id: Long): AnimeData? {
        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes WHERE id = ?", ah, id).firstOrNull()
    }

    private fun getAnime(connection: Connection?, name: String): AnimeData? {
        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes WHERE name = ?", ah, name).firstOrNull()
    }

    fun insertAnime(
        connection: Connection?,
        countryId: Long,
        releaseDate: String,
        name: String,
        image: String?,
        description: String?
    ): AnimeData? {
        val anime = getAnime(connection, name)

        return if (anime != null) anime
        else {
            var imagePath = image

            Impl.tryCatch("Failed to create anime image file") {
                val uuid = UUID.randomUUID()
                val bufferedImage = FileImpl.resizeImage(ImageIO.read(URL(image)), 350, 500)

                val localFile = File(FileImpl.directories("images", "animes"), "$uuid.jpg")
                ImageIO.write(bufferedImage, "jpg", localFile)
                imagePath = localFile.path
            }

            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query =
                "INSERT INTO animes (id, country_id, release_date, name, image, description) VALUES (NULL, ?, ?, ?, ?, ?)"
            val newId: Long =
                runner.insert(connection, query, sh, countryId, releaseDate, name, imagePath, description).toLong()
            getAnime(connection, newId)
        }
    }

    fun getAnimeGenres(connection: Connection?): MutableList<AnimeGenreData> {
        val agh = AnimeGenreHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM genres", agh)
    }

    private fun getAnimeGenres(connection: Connection?, animeId: Long, genre: String): AnimeGenreData? {
        val agh = AnimeGenreHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM genres WHERE anime_id = ? AND genre = ?", agh, animeId, genre)
            .firstOrNull()
    }

    fun insertAnimeGenre(connection: Connection?, animeId: Long, genre: String): AnimeGenreData? {
        val animeGenre = getAnimeGenres(connection, animeId, genre)

        return if (animeGenre != null) animeGenre
        else {
            val runner = QueryRunner()
            val query = "INSERT INTO genres (anime_id, genre) VALUES (?, ?)"
            runner.update(connection, query, animeId, genre)
            return getAnimeGenres(connection, animeId, genre)
        }
    }

    fun getEpisodes(connection: Connection?): MutableList<EpisodeData> {
        val blh = BeanListHandler(EpisodeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episodes", blh)
    }

    private fun getEpisode(connection: Connection?, id: Long): EpisodeData? {
        val blh = BeanListHandler(EpisodeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episodes WHERE id = ?", blh, id).firstOrNull()
    }

    private fun getEpisode(connection: Connection?, episodeId: String): EpisodeData? {
        val blh = BeanListHandler(EpisodeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episodes WHERE episode_id = ?", blh, episodeId).firstOrNull()
    }


    fun insertEpisode(
        connection: Connection?,
        animeId: Long,
        platformId: Long,
        releaseDate: String,
        season: Int,
        number: Int,
        episodeType: String,
        langType: String,
        episodeId: String,
        title: String?,
        url: String,
        image: String?,
        duration: Long
    ): EpisodeData? {
        val episode = getEpisode(connection, episodeId)

        return if (episode != null) episode
        else {
            var imagePath = image

            Impl.tryCatch("Failed to create episode image file") {
                val uuid = UUID.randomUUID()
                val bufferedImage = FileImpl.resizeImage(ImageIO.read(URL(image)), 640, 360)

                val localFile = File(FileImpl.directories("images", "episodes"), "$uuid.jpg")
                ImageIO.write(bufferedImage, "jpg", localFile)
                imagePath = localFile.path
            }

            var n = number

            if (!episodeType.equals(EpisodeType.EPISODE.name, true)) {
                val lastNumber = this.getAnime(
                    connection,
                    animeId
                )?.episodes?.filter { it.platformId == platformId && it.season == season && it.episodeType == episodeType && it.langType == it.langType }
                    ?.maxByOrNull { it.number }?.number
                n = (lastNumber ?: 0) + 1
            }

            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query =
                "INSERT INTO episodes (id, anime_id, platform_id, release_date, season, number, episode_type, lang_type, episode_id, title, url, image, duration) VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            val newId: Long = runner.insert(
                connection,
                query,
                sh,
                animeId,
                platformId,
                releaseDate,
                season,
                n,
                episodeType,
                langType,
                episodeId,
                title,
                url,
                imagePath,
                duration
            )
            getEpisode(connection, newId)
        }
    }
}