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
    fun get(connection: Connection?): MutableList<AnimeData> {
        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes", ah)
    }

    fun get(connection: Connection?, id: Long?): AnimeData? {
        val ah = AnimeHandler(connection)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM animes WHERE id = ?", ah, id).firstOrNull()
    }

    fun insert(
        connection: Connection?,
        animeCodeMapper: AnimeCodeMapper,
        countryId: Long?,
        releaseDate: String?,
        name: String?,
        image: String?,
        description: String?,
        saveImage: Boolean = true
    ): AnimeData? {
        val code = HashUtils.sha512(name?.lowercase()?.onlyLettersAndDigits())
        val animeCode = animeCodeMapper.get(connection, code)

        return if (animeCode != null) {
            var anime = get(connection, animeCode.animeId)

            if (anime?.description?.trim().isNullOrEmpty() && !description?.trim().isNullOrEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE animes SET description = ? WHERE id = ?"
                runner.update(connection, query, description, anime?.id)
                anime = get(connection, anime?.id)
            }

            if ((anime?.image?.trim().isNullOrEmpty() || anime?.image?.trim()?.startsWith("http") == true) && !image?.trim().isNullOrEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE animes SET image = ? WHERE id = ?"
                runner.update(connection, query, saveAnimeImage(image, saveImage), anime?.id)
                anime = get(connection, anime?.id)
            }

            anime
        } else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query =
                "INSERT INTO animes (country_id, release_date, name, image, description) VALUES (?, ?, ?, ?, ?)"
            val newId: Long =
                runner.insert(
                    connection,
                    query,
                    sh,
                    countryId,
                    releaseDate,
                    name?.trim(),
                    saveAnimeImage(image, saveImage),
                    description
                )
                    .toLong()
            val anime = get(connection, newId)
            animeCodeMapper.insert(connection, anime?.id, code)
            anime
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