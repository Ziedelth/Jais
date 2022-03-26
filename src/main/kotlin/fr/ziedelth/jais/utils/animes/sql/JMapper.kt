/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.HashUtils
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.*
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.sql.data.*
import fr.ziedelth.jais.utils.animes.sql.handlers.*
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

    /**
     * It takes a connection and returns a list of CountryData objects
     *
     * @param connection The connection to the database.
     * @return A list of CountryData objects.
     */
    fun getCountries(connection: Connection?): MutableList<CountryData> {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries", blh)
    }

    /**
     * Get a country by id
     *
     * @param connection The connection to the database.
     * @param id The id of the country to retrieve.
     * @return Nothing.
     */
    fun getCountry(connection: Connection?, id: Long): CountryData? {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries WHERE id = ?", blh, id).firstOrNull()
    }

    /**
     * Get a country by its tag
     *
     * @param connection The connection to the database.
     * @param tag The tag of the country you want to get.
     * @return A CountryData object.
     */
    fun getCountryByTag(connection: Connection?, tag: String): CountryData? {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries WHERE tag = ?", blh, tag).firstOrNull()
    }

    /**
     * Get a country by name
     *
     * @param connection The connection to the database.
     * @param name The name of the country to retrieve.
     * @return Nothing.
     */
    fun getCountryByName(connection: Connection?, name: String?): CountryData? {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries WHERE name = ?", blh, name).firstOrNull()
    }

    /**
     * Insert a country into the database
     *
     * @param connection The connection to the database.
     * @param countryHandler CountryHandler is the object that holds the data for the country.
     */
    fun insertCountry(connection: Connection?, countryHandler: CountryHandler): CountryData? =
        insertCountry(connection, countryHandler.tag, countryHandler.name, countryHandler.flag, countryHandler.season)

    /**
     * If the country already exists, return it. Otherwise, insert it into the database and return it
     *
     * @param connection The connection to the database.
     * @param tag The country's tag, which is a unique identifier.
     * @param name The name of the country.
     * @param flag The flag of the country.
     * @param season The season the country is in.
     * @return A CountryData object.
     */
    fun insertCountry(connection: Connection?, tag: String, name: String, flag: String, season: String): CountryData? {
        val country = getCountryByTag(connection, tag)

        return if (country != null) country
        else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO countries (tag, name, flag, season) VALUES (?, ?, ?, ?)"
            val newId: Long = runner.insert(
                connection,
                query,
                sh,
                tag,
                name,
                flag,
                season
            ).toLong()
            getCountry(connection, newId)
        }
    }

    /**
     * It returns a list of PlatformData objects.
     *
     * @param connection The connection to the database.
     * @return A list of PlatformData objects.
     */
    fun getPlatforms(connection: Connection?): MutableList<PlatformData> {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms", blh)
    }

    /**
     * It returns a PlatformData object for the given id.
     *
     * @param connection The connection to the database.
     * @param id The id of the platform to retrieve.
     * @return A PlatformData object.
     */
    fun getPlatform(connection: Connection?, id: Long): PlatformData? {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms WHERE id = ?", blh, id).firstOrNull()
    }

    /**
     * Get a platform by name
     *
     * @param connection The connection to the database.
     * @param name The name of the platform to retrieve.
     * @return A PlatformData object.
     */
    fun getPlatform(connection: Connection?, name: String?): PlatformData? {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms WHERE name = ?", blh, name).firstOrNull()
    }

    /**
     * Insert a platform into the database
     *
     * @param connection The connection to the database.
     * @param platformHandler PlatformHandler is the object that holds the data for the platform.
     */
    fun insertPlatform(connection: Connection?, platformHandler: PlatformHandler): PlatformData? = insertPlatform(
        connection,
        platformHandler.name,
        platformHandler.url,
        platformHandler.image,
        platformHandler.color
    )

    /**
     * If the platform already exists, return it. Otherwise, insert it into the database and return it
     *
     * @param connection The connection to the database.
     * @param name The name of the platform.
     * @param url The URL of the platform.
     * @param image The image of the platform.
     * @param color The color of the platform.
     * @return A PlatformData object.
     */
    fun insertPlatform(connection: Connection?, name: String, url: String, image: String, color: Int): PlatformData? {
        val platform = getPlatform(connection, name)

        return if (platform != null) platform
        else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO platforms (name, url, image, color) VALUES (?, ?, ?, ?)"
            val newId: Long = runner.insert(
                connection,
                query,
                sh,
                name,
                url,
                image,
                color
            ).toLong()
            getPlatform(connection, newId)
        }
    }

    /**
     * Get all the genres from the database
     *
     * @param connection The connection to the database.
     * @return A list of GenreData objects.
     */
    fun getGenres(connection: Connection?): MutableList<GenreData> {
        val blh = BeanListHandler(GenreData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM genres", blh)
    }

    /**
     * Get a genre by its ID
     *
     * @param connection The connection to the database.
     * @param id The id of the genre to retrieve.
     * @return A GenreData object.
     */
    fun getGenre(connection: Connection?, id: Long): GenreData? {
        val blh = BeanListHandler(GenreData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM genres WHERE id = ?", blh, id).firstOrNull()
    }

    /**
     * Get a genre by name
     *
     * @param connection The connection to the database.
     * @param name The name of the genre.
     * @return A GenreData object.
     */
    fun getGenre(connection: Connection?, name: String?): GenreData? {
        val blh = BeanListHandler(GenreData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM genres WHERE `name` = ?", blh, name).firstOrNull()
    }

    /**
     * If the genre already exists, update the genre's name and/or fr field. If the genre doesn't exist, insert it
     *
     * @param connection The connection to the database.
     * @param agenre Genre
     * @return A GenreData object.
     */
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

    /**
     * It returns a list of EpisodeTypeData objects.
     *
     * @param connection The connection to the database.
     * @return A list of EpisodeTypeData objects.
     */
    fun getEpisodeTypes(connection: Connection?): MutableList<EpisodeTypeData> {
        val blh = BeanListHandler(EpisodeTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episode_types", blh)
    }

    /**
     * It gets the episode type for the given id.
     *
     * @param connection The connection to the database.
     * @param id The id of the episode type to be retrieved.
     * @return A list of EpisodeTypeData objects.
     */
    private fun getEpisodeType(connection: Connection?, id: Long): EpisodeTypeData? {
        val blh = BeanListHandler(EpisodeTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episode_types WHERE id = ?", blh, id).firstOrNull()
    }

    /**
     * It returns the episode type data for the given name.
     *
     * @param connection The connection to the database.
     * @param name The name of the episode type.
     * @return A list of EpisodeTypeData objects.
     */
    fun getEpisodeType(connection: Connection?, name: String): EpisodeTypeData? {
        val blh = BeanListHandler(EpisodeTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episode_types WHERE `name` = ?", blh, name).firstOrNull()
    }

    /**
     * If the episode type already exists, update the French translation if it's not empty. Otherwise, return the existing
     * episode type
     *
     * @param connection The connection to the database.
     * @param aepisodeType The EpisodeType object that you want to insert.
     * @return The episode type that was inserted.
     */
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

    /**
     * It takes a connection and returns a list of LangTypeData objects
     *
     * @param connection The connection to the database.
     * @return A list of LangTypeData objects.
     */
    fun getLangTypes(connection: Connection?): MutableList<LangTypeData> {
        val blh = BeanListHandler(LangTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM lang_types", blh)
    }

    /**
     * Get a single LangTypeData object from the database by id
     *
     * @param connection The connection to the database.
     * @param id The id of the LangTypeData object to be retrieved.
     * @return A `LangTypeData` object.
     */
    private fun getLangType(connection: Connection?, id: Long): LangTypeData? {
        val blh = BeanListHandler(LangTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM lang_types WHERE id = ?", blh, id).firstOrNull()
    }

    /**
     * It returns a LangTypeData object for the given name.
     *
     * @param connection The database connection to use.
     * @param name The name of the language type.
     * @return A list of LangTypeData objects.
     */
    fun getLangType(connection: Connection?, name: String): LangTypeData? {
        val blh = BeanListHandler(LangTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM lang_types WHERE `name` = ?", blh, name).firstOrNull()
    }

    /**
     * If the language type already exists, update the French translation if it's not empty. Otherwise, return the existing
     * language type
     *
     * @param connection The connection to the database.
     * @param alangType The LangType object that you want to insert.
     * @return A LangTypeData object.
     */
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

    /**
     * It takes a connection and returns a list of anime data
     *
     * @param connection The connection to the database.
     * @return A list of AnimeData objects.
     */
    fun getAnimes(connection: Connection?): MutableList<AnimeData> {
        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes", ah)
    }

    /**
     * Get an anime by its id
     *
     * @param connection The connection to the database.
     * @param id The id of the anime you want to get.
     * @return A single AnimeData object.
     */
    fun getAnime(connection: Connection?, id: Long?): AnimeData? {
        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes WHERE id = ?", ah, id).firstOrNull()
    }

    /**
     * Get an anime by country id and name
     *
     * @param connection The connection to the database.
     * @param countryId The country ID of the anime.
     * @param name The name of the anime.
     * @return An AnimeData object.
     */
    fun getAnime(connection: Connection?, countryId: Long?, name: String?): AnimeData? {
        val code = HashUtils.sha512(name?.lowercase()?.onlyLettersAndDigits())

        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes WHERE country_id = ? AND code = ?", ah, countryId, code)
            .firstOrNull()
    }

    /**
     * Insert an anime into the database
     *
     * @param connection The connection to the database.
     * @param countryId The country ID of the anime.
     * @param releaseDate The date the anime was released.
     * @param name The name of the anime.
     * @param image The image of the anime.
     * @param description The description of the anime.
     * @param saveImage Boolean = true
     * @return The anime object that was inserted.
     */
    fun insertAnime(
        connection: Connection?,
        countryId: Long?,
        releaseDate: String?,
        name: String?,
        image: String?,
        description: String?,
        saveImage: Boolean = true
    ): AnimeData? {
        var anime = getAnime(connection, countryId, name)

        return if (anime != null) {
            if (anime.description.isNullOrEmpty() && !description.isNullOrEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE animes SET description = ? WHERE id = ?"
                runner.update(connection, query, description, anime.id)
                anime = getAnime(connection, anime.id)
            }

            if (anime?.image.isNullOrEmpty() && !image.isNullOrEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE animes SET image = ? WHERE id = ?"
                runner.update(connection, query, saveAnimeImage(image, saveImage), anime?.id)
                anime = getAnime(connection, anime?.id)
            }

            anime
        } else {
            val code = HashUtils.sha512(name?.lowercase()?.onlyLettersAndDigits())

            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query =
                "INSERT INTO animes (country_id, release_date, code, name, image, description) VALUES (?, ?, ?, ?, ?, ?)"
            val newId: Long =
                runner.insert(
                    connection,
                    query,
                    sh,
                    countryId,
                    releaseDate,
                    code,
                    name,
                    saveAnimeImage(image, saveImage),
                    description
                )
                    .toLong()
            getAnime(connection, newId)
        }
    }

    /**
     * If the image is not null, resize it to 350x500 and save it to the local file system. If the image is null, return
     * null
     *
     * @param image The image URL.
     * @param save If true, the image will be saved to the local file system.
     * @return The image path.
     */
    private fun saveAnimeImage(image: String?, save: Boolean): String? {
        var imagePath = image

        if (save) {
            Impl.tryCatch("Failed to create anime image file") {
                val uuid = UUID.randomUUID()
                val bufferedImage = FileImpl.resizeImage(ImageIO.read(URL(image)), 350, 500)

                val fileName = "$uuid.jpg"
                val localFile = File(FileImpl.directories(true, "images", "animes"), fileName)
                ImageIO.write(bufferedImage, "jpg", localFile)
                val webFile = File(FileImpl.directories(false, "/var/www/html/images/animes"), fileName)
                ImageIO.write(bufferedImage, "jpg", webFile)

                imagePath = "images/animes/$fileName"
            }
        }

        return imagePath
    }

    /**
     * It takes a connection as a parameter, and returns a list of anime genres
     *
     * @param connection The connection to the database.
     * @return A list of AnimeGenreData objects.
     */
    fun getAnimeGenres(connection: Connection?): MutableList<AnimeGenreData> {
        val agh = AnimeGenreHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM anime_genres", agh)
    }

    /**
     * It takes a connection, animeId, and genreId and returns an AnimeGenreData object
     *
     * @param connection The connection to the database.
     * @param animeId The id of the anime you want to get the genre for.
     * @param genreId The id of the genre.
     * @return The AnimeGenreData object.
     */
    fun getAnimeGenres(connection: Connection?, animeId: Long?, genreId: Long?): AnimeGenreData? {
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

    /**
     * It takes a connection and an anime ID, and returns a list of anime genres
     *
     * @param connection The connection to the database.
     * @param animeId The id of the anime you want to get the genres for.
     * @return A list of AnimeGenreData objects.
     */
    fun getAnimeGenres(connection: Connection?, animeId: Long?): MutableList<AnimeGenreData>? {
        val agh = AnimeGenreHandler()
        val runner = QueryRunner()
        return runner.query(
            connection,
            "SELECT * FROM anime_genres WHERE anime_id = ?",
            agh,
            animeId
        )
    }

    /**
     * If the anime genre already exists, return it. Otherwise, insert it and return it
     *
     * @param connection The connection to the database.
     * @param animeId The id of the anime.
     * @param genreId The id of the genre to be inserted.
     * @return The AnimeGenreData object that was inserted.
     */
    fun insertAnimeGenre(connection: Connection?, animeId: Long?, genreId: Long?): AnimeGenreData? {
        val animeGenre = getAnimeGenres(connection, animeId, genreId)

        return if (animeGenre != null) animeGenre
        else {
            val runner = QueryRunner()
            val query = "INSERT INTO anime_genres (anime_id, genre_id) VALUES (?, ?)"
            runner.update(connection, query, animeId, genreId)
            return getAnimeGenres(connection, animeId, genreId)
        }
    }

    /**
     * It takes a connection and returns a list of EpisodeData objects
     *
     * @param connection The connection to the database.
     * @return A list of EpisodeData objects.
     */
    fun getEpisodes(connection: Connection?): MutableList<EpisodeData> {
        val episodeHandler = EpisodeHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episodes", episodeHandler)
    }

    /**
     * Get the episode with the given id
     *
     * @param connection The connection to the database.
     * @param id The id of the episode to get.
     * @return Nothing.
     */
    fun getEpisode(connection: Connection?, id: Long?): EpisodeData? {
        val episodeHandler = EpisodeHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episodes WHERE id = ?", episodeHandler, id).firstOrNull()
    }

    /**
     * Get the episode data for the episode with the given id
     *
     * @param connection The connection to the database.
     * @param episodeId The episode ID of the episode you want to get.
     * @return Nothing.
     */
    fun getEpisode(connection: Connection?, episodeId: String): EpisodeData? {
        val episodeHandler = EpisodeHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM episodes WHERE episode_id = ?", episodeHandler, episodeId)
            .firstOrNull()
    }

    /**
     * Inserts an episode into the database
     *
     * @param connection The connection to the database.
     * @param platformId The platform ID of the episode.
     * @param animeId The ID of the anime.
     * @param idEpisodeType The ID of the episode type.
     * @param idLangType The language type of the episode.
     * @param releaseDate The date the episode was released.
     * @param season The season number.
     * @param number The number of the episode.
     * @param episodeId The unique identifier for the episode.
     * @param title The title of the episode.
     * @param url The URL of the episode.
     * @param image The image URL.
     * @param duration The duration of the episode in seconds.
     * @param saveImage Boolean = true
     * @return The episode that was just inserted.
     */
    fun insertEpisode(
        connection: Connection?,
        platformId: Long?,
        animeId: Long?,
        idEpisodeType: Long?,
        idLangType: Long?,
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
                    ImageIO.write(bufferedImage, "jpg", localFile)
                    val webFile = File(FileImpl.directories(false, "/var/www/html/images/episodes"), fileName)
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

    /**
     * It takes a connection and returns a list of ScanData objects
     *
     * @param connection The connection to the database.
     * @return A list of ScanData objects.
     */
    fun getScans(connection: Connection?): MutableList<ScanData> {
        val scanHandler = ScanHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM scans", scanHandler)
    }

    /**
     * Get a ScanData object from the database by its id
     *
     * @param connection The connection to the database.
     * @param id The id of the scan to retrieve.
     * @return A ScanData object.
     */
    fun getScan(connection: Connection?, id: Long?): ScanData? {
        val scanHandler = ScanHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM scans WHERE id = ?", scanHandler, id).firstOrNull()
    }

    /**
     * It takes a connection, a platformId, an animeId, and a number, and returns a ScanData object
     *
     * @param connection The connection to the database.
     * @param platformId The platform ID of the scan.
     * @param animeId The id of the anime that the scan is for.
     * @param number The number of the scan.
     * @return A ScanData object.
     */
    fun getScan(connection: Connection?, platformId: Long?, animeId: Long?, number: String): ScanData? {
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

    /**
     * Insert a new scan into the database
     *
     * @param connection The connection to the database.
     * @param platformId The platform ID of the scan.
     * @param animeId The id of the anime.
     * @param idEpisodeType The episode type ID.
     * @param idLangType The language type of the scan.
     * @param releaseDate The date the scan was released.
     * @param number The number of the scan.
     * @param url The URL of the scan.
     * @return A ScanData object.
     */
    fun insertScan(
        connection: Connection?,
        platformId: Long?,
        animeId: Long?,
        idEpisodeType: Long?,
        idLangType: Long?,
        releaseDate: String,
        number: String,
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

    /**
     * It returns a list of OpsEndsTypeData objects.
     *
     * @param connection The connection to the database.
     * @return A list of OpsEndsTypeData objects.
     */
    fun getOpsEndsTypes(connection: Connection?): MutableList<OpsEndsTypeData> {
        val episodeHandler = BeanListHandler(OpsEndsTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM ops_ends_types", episodeHandler)
    }

    /**
     * It takes a connection as a parameter and returns a list of OpsEndsData objects
     *
     * @param connection The connection to the database.
     * @return A list of OpsEndsData objects.
     */
    fun getOpsEnds(connection: Connection?): MutableList<OpsEndsData> {
        val episodeHandler = OpsEndsHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM ops_ends", episodeHandler)
    }

    /**
     * Inserts an anime's opsEnds into the database
     *
     * @param connection The connection to the database.
     * @param animeId The id of the anime that the opsEnds is for.
     * @param opsEndsType The type of opsEnds.
     * @param url The url of the ops/ends.
     */
    fun insertOpsEnds(
        connection: Connection?,
        animeId: Long,
        opsEndsType: Long,
        url: String,
    ) {
        val sh = ScalarHandler<Long>()
        val runner = QueryRunner()
        val query = "INSERT INTO ops_ends (anime_id, ops_ends_type_id, url) VALUES (?, ?, ?)"
        runner.insert(
            connection,
            query,
            sh,
            animeId,
            opsEndsType,
            url
        )
    }

    /**
     * Insert an episode into the database
     *
     * @param connection The connection to the database.
     * @param episode The Episode object that we want to insert.
     * @return Nothing.
     */
    fun insertEpisode(connection: Connection?, episode: Episode): EpisodeData? {
        val platformData = insertPlatform(connection, episode.platform.platformHandler)
        val countryData = insertCountry(connection, episode.country.countryHandler)

        val animeData = insertAnime(
            connection,
            countryData?.id,
            ISO8601.toUTCDate(ISO8601.fromCalendar(episode.releaseDate)),
            episode.anime,
            episode.animeImage,
            episode.animeDescription
        )

        if (animeData?.genres?.isEmpty() == true) {
            episode.animeGenres.forEach {
                val genre = insertGenre(connection, it)
                if (genre != null)
                    insertAnimeGenre(
                        connection,
                        animeData.id,
                        genre.id
                    )
            }
        }

        return insertEpisode(
            connection,
            platformData?.id,
            animeData?.id,
            insertEpisodeType(connection, episode.episodeType)?.id,
            insertLangType(connection, episode.langType)?.id,
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
        val platformData = insertPlatform(connection, scan.platform.platformHandler)
        val countryData = insertCountry(connection, scan.country.countryHandler)

        val animeData = insertAnime(
            connection,
            countryData?.id,
            ISO8601.toUTCDate(ISO8601.fromCalendar(scan.releaseDate)),
            scan.anime,
            scan.animeImage,
            scan.animeDescription
        )

        if (animeData?.genres?.isEmpty() == true) {
            scan.animeGenres.forEach {
                val genre = insertGenre(connection, it)
                if (genre != null)
                    insertAnimeGenre(
                        connection,
                        animeData.id,
                        genre.id
                    )
            }
        }

        return insertScan(
            connection,
            platformData?.id,
            animeData?.id,
            insertEpisodeType(connection, scan.episodeType)?.id,
            insertLangType(connection, scan.langType)?.id,
            ISO8601.toUTCDate(ISO8601.fromCalendar(scan.releaseDate)),
            scan.number,
            scan.url
        )
    }
}