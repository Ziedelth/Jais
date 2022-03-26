/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.HashUtils
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.sql.data.AnimeData
import fr.ziedelth.jais.utils.animes.sql.handlers.AnimeHandler
import fr.ziedelth.jais.utils.plugins.PluginUtils.onlyLettersAndDigits
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.io.File
import java.net.URL
import java.sql.Connection
import java.util.*
import javax.imageio.ImageIO

class AnimeMapper {
    /**
     * It takes a connection and returns a list of anime data
     *
     * @param connection The connection to the database.
     * @return A list of AnimeData objects.
     */
    fun get(connection: Connection?): MutableList<AnimeData> {
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
    fun get(connection: Connection?, id: Long?): AnimeData? {
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
    fun get(connection: Connection?, countryId: Long?, name: String?): AnimeData? {
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
    fun insert(
        connection: Connection?,
        countryId: Long?,
        releaseDate: String?,
        name: String?,
        image: String?,
        description: String?,
        saveImage: Boolean = true
    ): AnimeData? {
        var anime = get(connection, countryId, name)

        return if (anime != null) {
            if (anime.description.isNullOrEmpty() && !description.isNullOrEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE animes SET description = ? WHERE id = ?"
                runner.update(connection, query, description, anime.id)
                anime = get(connection, anime.id)
            }

            if (anime?.image.isNullOrEmpty() && !image.isNullOrEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE animes SET image = ? WHERE id = ?"
                runner.update(connection, query, saveAnimeImage(image, saveImage), anime?.id)
                anime = get(connection, anime?.id)
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
            get(connection, newId)
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
}