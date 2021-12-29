/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.HashUtils
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.LangType
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.sql.data.*
import fr.ziedelth.jais.utils.animes.sql.handlers.AnimeGenreHandler
import fr.ziedelth.jais.utils.animes.sql.handlers.AnimeHandler
import fr.ziedelth.jais.utils.animes.sql.handlers.EpisodeHandler
import fr.ziedelth.jais.utils.animes.sql.handlers.ScanHandler
import fr.ziedelth.jais.utils.plugins.PluginUtils.onlyLettersAndDigits
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanListHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.io.File
import java.net.URL
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import javax.imageio.ImageIO

object JMapper {
    fun getConnection(): Connection? {
        val configuration = Configuration.load() ?: return null
        return DriverManager.getConnection(configuration.url, configuration.user, configuration.password)
    }

    fun getCountries(connection: Connection?): MutableList<CountryData> {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries", blh)
    }

    fun getCountry(connection: Connection?, id: Long): CountryData? {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries WHERE id = ?", blh, id).firstOrNull()
    }

    fun getCountry(connection: Connection?, tag: String): CountryData? {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries WHERE tag = ?", blh, tag).firstOrNull()
    }

    fun insertCountry(connection: Connection?, countryHandler: CountryHandler): CountryData? {
        val country = getCountry(connection, countryHandler.tag)

        return if (country != null) country
        else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO countries (tag, name, flag, season) VALUES (?, ?, ?, ?)"
            val newId: Long = runner.insert(
                connection,
                query,
                sh,
                countryHandler.tag,
                countryHandler.name,
                countryHandler.flag,
                countryHandler.season
            ).toLong()
            getCountry(connection, newId)
        }
    }

    fun getPlatforms(connection: Connection?): MutableList<PlatformData> {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms", blh)
    }

    fun getPlatform(connection: Connection?, id: Long): PlatformData? {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms WHERE id = ?", blh, id).firstOrNull()
    }

    fun getPlatform(connection: Connection?, name: String): PlatformData? {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms WHERE name = ?", blh, name).firstOrNull()
    }

    fun insertPlatform(connection: Connection?, platformHandler: PlatformHandler): PlatformData? {
        val platform = getPlatform(connection, platformHandler.name)

        return if (platform != null) platform
        else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO platforms (name, url, image, color) VALUES (?, ?, ?, ?)"
            val newId: Long = runner.insert(
                connection,
                query,
                sh,
                platformHandler.name,
                platformHandler.url,
                platformHandler.image,
                platformHandler.color
            ).toLong()
            getPlatform(connection, newId)
        }
    }

    fun getGenres(connection: Connection?): MutableList<GenreData> {
        val blh = BeanListHandler(GenreData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM genres", blh)
    }

    private fun getGenre(connection: Connection?, id: Long): GenreData? {
        val blh = BeanListHandler(GenreData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM genres WHERE id = ?", blh, id).firstOrNull()
    }

    fun getGenre(connection: Connection?, name: String): GenreData? {
        val blh = BeanListHandler(GenreData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM genres WHERE `name` = ?", blh, name).firstOrNull()
    }

    fun insertGenre(connection: Connection?, agenre: Genre): GenreData? {
        val genre = getGenre(connection, agenre.name)

        return if (genre != null) {
            if (genre.fr.isEmpty() && agenre.fr.isNotEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE genres SET fr = ? WHERE id = ?"
                runner.update(connection, query, agenre.fr, genre.id)
                getGenre(connection, genre.id)
            } else genre
        } else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO genres (name, fr) VALUES (?, ?)"
            val newId: Long = runner.insert(connection, query, sh, agenre.name, agenre.fr).toLong()
            getGenre(connection, newId)
        }
    }

    fun getEpisodeTypes(connection: Connection?): MutableList<EpisodeTypeData> {
        val blh = BeanListHandler(EpisodeTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episode_types", blh)
    }

    private fun getEpisodeType(connection: Connection?, id: Long): EpisodeTypeData? {
        val blh = BeanListHandler(EpisodeTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episode_types WHERE id = ?", blh, id).firstOrNull()
    }

    fun getEpisodeType(connection: Connection?, name: String): EpisodeTypeData? {
        val blh = BeanListHandler(EpisodeTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episode_types WHERE `name` = ?", blh, name).firstOrNull()
    }

    fun insertEpisodeType(connection: Connection?, aepisodeType: EpisodeType): EpisodeTypeData? {
        val episodeType = getEpisodeType(connection, aepisodeType.name)

        return if (episodeType != null) {
            if (episodeType.fr.isEmpty() && aepisodeType.fr.isNotEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE episode_types SET fr = ? WHERE id = ?"
                runner.update(connection, query, aepisodeType.fr, episodeType.id)
                getEpisodeType(connection, episodeType.id)
            } else episodeType
        } else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO episode_types (name, fr) VALUES (?, ?)"
            val newId: Long = runner.insert(connection, query, sh, aepisodeType.name, aepisodeType.fr).toLong()
            getEpisodeType(connection, newId)
        }
    }

    fun getLangTypes(connection: Connection?): MutableList<LangTypeData> {
        val blh = BeanListHandler(LangTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM lang_types", blh)
    }

    private fun getLangType(connection: Connection?, id: Long): LangTypeData? {
        val blh = BeanListHandler(LangTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM lang_types WHERE id = ?", blh, id).firstOrNull()
    }

    fun getLangType(connection: Connection?, name: String): LangTypeData? {
        val blh = BeanListHandler(LangTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM lang_types WHERE `name` = ?", blh, name).firstOrNull()
    }

    fun insertLangType(connection: Connection?, alangType: LangType): LangTypeData? {
        val langType = getLangType(connection, alangType.name)

        return if (langType != null) {
            if (langType.fr.isEmpty() && alangType.fr.isNotEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE lang_types SET fr = ? WHERE id = ?"
                runner.update(connection, query, alangType.fr, langType.id)
                getLangType(connection, langType.id)
            } else langType
        } else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO lang_types (name, fr) VALUES (?, ?)"
            val newId: Long = runner.insert(connection, query, sh, alangType.name, alangType.fr).toLong()
            getLangType(connection, newId)
        }
    }

    fun getAnimes(connection: Connection?): MutableList<AnimeData> {
        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes", ah)
    }

    fun getAnime(connection: Connection?, id: Long): AnimeData? {
        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes WHERE id = ?", ah, id).firstOrNull()
    }

    fun getAnime(connection: Connection?, countryId: Long, name: String): AnimeData? {
        val code = HashUtils.sha512(name.lowercase().onlyLettersAndDigits())

        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes WHERE country_id = ? AND code = ?", ah, countryId, code)
            .firstOrNull()
    }

    fun insertAnime(
        connection: Connection?,
        countryId: Long,
        releaseDate: String,
        name: String,
        image: String?,
        description: String?,
        saveImage: Boolean = true
    ): AnimeData? {
        val anime = getAnime(connection, countryId, name)

        return if (anime != null) {
            if (anime.description.isNullOrEmpty() && !description.isNullOrEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE animes SET description = ? WHERE id = ?"
                runner.update(connection, query, description, anime.id)
                getAnime(connection, anime.id)
            } else anime
        } else {
            val code = HashUtils.sha512(name.lowercase().onlyLettersAndDigits())
            var imagePath = image

            if (saveImage) {
                Impl.tryCatch("Failed to create anime image file") {
                    val uuid = UUID.randomUUID()
                    val bufferedImage = FileImpl.resizeImage(ImageIO.read(URL(image)), 350, 500)

                    val fileName = "$uuid.jpg"
                    val localFile = File(FileImpl.directories(true, "images", "animes"), fileName)
                    val webFile = File(FileImpl.directories(false, "/var/www/ziedelth.fr/images/animes"), fileName)
                    ImageIO.write(bufferedImage, "jpg", localFile)
                    ImageIO.write(bufferedImage, "jpg", webFile)

                    imagePath = "images/animes/$fileName"
                }
            }

            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query =
                "INSERT INTO animes (country_id, release_date, code, name, image, description) VALUES (?, ?, ?, ?, ?, ?)"
            val newId: Long =
                runner.insert(connection, query, sh, countryId, releaseDate, code, name, imagePath, description)
                    .toLong()
            getAnime(connection, newId)
        }
    }

    fun getAnimeGenres(connection: Connection?): MutableList<AnimeGenreData> {
        val agh = AnimeGenreHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM anime_genres", agh)
    }

    fun getAnimeGenres(connection: Connection?, animeId: Long, genreId: Long): AnimeGenreData? {
        val agh = AnimeGenreHandler()
        val runner = QueryRunner()
        return runner.query(
            connection,
            "SELECT * FROM anime_genres WHERE anime_id = ? AND genre_id = ?",
            agh,
            animeId,
            genreId
        ).firstOrNull()
    }

    fun insertAnimeGenre(connection: Connection?, animeId: Long, genreId: Long): AnimeGenreData? {
        val animeGenre = getAnimeGenres(connection, animeId, genreId)

        return if (animeGenre != null) animeGenre
        else {
            val runner = QueryRunner()
            val query = "INSERT INTO anime_genres (anime_id, genre_id) VALUES (?, ?)"
            runner.update(connection, query, animeId, genreId)
            return getAnimeGenres(connection, animeId, genreId)
        }
    }

    fun getEpisodes(connection: Connection?): MutableList<EpisodeData> {
        val episodeHandler = EpisodeHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episodes", episodeHandler)
    }

    fun getEpisode(connection: Connection?, id: Long): EpisodeData? {
        val episodeHandler = EpisodeHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episodes WHERE id = ?", episodeHandler, id).firstOrNull()
    }

    fun getEpisode(connection: Connection?, episodeId: String): EpisodeData? {
        val episodeHandler = EpisodeHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episodes WHERE episode_id = ?", episodeHandler, episodeId)
            .firstOrNull()
    }

    fun insertEpisode(
        connection: Connection?,
        platformId: Long,
        animeId: Long,
        idEpisodeType: Long,
        idLangType: Long,
        releaseDate: String,
        season: Int,
        number: Int,
        episodeId: String,
        title: String?,
        url: String,
        image: String,
        duration: Long,
        saveImage: Boolean = true
    ): EpisodeData? {
        var episode = getEpisode(connection, episodeId)

        return if (episode != null) {
            if (episode.title.isNullOrBlank() || !episode.title.equals(title, true)) {
                val runner = QueryRunner()
                val query = "UPDATE episodes SET title = ? WHERE id = ?"
                runner.update(connection, query, title, episode.id)
                episode = getEpisode(connection, episode.id)
            }

            if (episode != null && (episode.url.isBlank() || !episode.url.equals(url, true))) {
                val runner = QueryRunner()
                val query = "UPDATE episodes SET url = ? WHERE id = ?"
                runner.update(connection, query, url, episode.id)
                episode = getEpisode(connection, episode.id)
            }

            if (episode != null && episode.duration != duration) {
                val runner = QueryRunner()
                val query = "UPDATE episodes SET duration = ? WHERE id = ?"
                runner.update(connection, query, duration, episode.id)
                episode = getEpisode(connection, episode.id)
            }

            episode
        } else {
            var imagePath = image

            if (saveImage) {
                Impl.tryCatch("Failed to create episode image file") {
                    val uuid = UUID.randomUUID()
                    val bufferedImage = FileImpl.resizeImage(ImageIO.read(URL(image)), 640, 360)

                    val fileName = "$uuid.jpg"
                    val localFile = File(FileImpl.directories(true, "images", "episodes"), fileName)
                    val webFile = File(FileImpl.directories(false, "/var/www/ziedelth.fr/images/episodes"), fileName)
                    ImageIO.write(bufferedImage, "jpg", localFile)
                    ImageIO.write(bufferedImage, "jpg", webFile)

                    imagePath = "images/episodes/$fileName"
                }
            }

            var n = number

            if (n == -1) {
                val lastNumber = this.getAnime(
                    connection,
                    animeId
                )?.episodes?.filter { it.platformId == platformId && it.animeId == animeId && it.season == season && it.idEpisodeType == idEpisodeType && it.idLangType == idLangType }
                    ?.maxByOrNull { it.number }?.number
                n = (lastNumber ?: 0) + 1
            }

            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query =
                "INSERT INTO episodes (platform_id, anime_id, id_episode_type, id_lang_type, release_date, season, number, episode_id, title, url, image, duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            val newId: Long = runner.insert(
                connection,
                query,
                sh,
                platformId,
                animeId,
                idEpisodeType,
                idLangType,
                releaseDate,
                season,
                n,
                episodeId,
                title,
                url,
                imagePath,
                duration
            )

            getEpisode(connection, newId)
        }
    }

    fun getScans(connection: Connection?): MutableList<ScanData> {
        val scanHandler = ScanHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM scans", scanHandler)
    }

    fun getScan(connection: Connection?, id: Long): ScanData? {
        val scanHandler = ScanHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM scans WHERE id = ?", scanHandler, id).firstOrNull()
    }

    fun getScan(connection: Connection?, platformId: Long, animeId: Long, number: Int): ScanData? {
        val scanHandler = ScanHandler()
        val runner = QueryRunner()
        return runner.query(
            connection,
            "SELECT * FROM scans WHERE platform_id = ? AND anime_id = ? AND number = ?",
            scanHandler,
            platformId,
            animeId,
            number
        ).firstOrNull()
    }

    fun insertScan(
        connection: Connection?,
        platformId: Long,
        animeId: Long,
        idEpisodeType: Long,
        idLangType: Long,
        releaseDate: String,
        number: Int,
        url: String,
    ): ScanData? {
        val scan = getScan(connection, platformId, animeId, number)

        return if (scan != null) scan
        else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query =
                "INSERT INTO scans (platform_id, anime_id, id_episode_type, id_lang_type, release_date, number, url) VALUES (?, ?, ?, ?, ?, ?, ?)"
            val newId: Long = runner.insert(
                connection,
                query,
                sh,
                platformId,
                animeId,
                idEpisodeType,
                idLangType,
                releaseDate,
                number,
                url
            )

            getScan(connection, newId)
        }
    }
}