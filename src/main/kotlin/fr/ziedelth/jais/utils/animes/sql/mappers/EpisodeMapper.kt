/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.sql.data.EpisodeData
import fr.ziedelth.jais.utils.animes.sql.handlers.EpisodeHandler
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.io.File
import java.net.URL
import java.sql.Connection
import java.util.*
import javax.imageio.ImageIO

class EpisodeMapper {
    /**
     * It takes a connection and returns a list of EpisodeData objects
     *
     * @param connection The connection to the database.
     * @return A list of EpisodeData objects.
     */
    fun get(connection: Connection?): MutableList<EpisodeData> {
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
    fun get(connection: Connection?, id: Long?): EpisodeData? {
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
    fun get(connection: Connection?, episodeId: String): EpisodeData? {
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
    fun insert(
        connection: Connection?,
        animeMapper: AnimeMapper,
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
        var episode = get(connection, episodeId)

        return if (episode != null) {
            if (episode.title.isNullOrBlank() || !episode.title.equals(title, true)) {
                val runner = QueryRunner()
                val query = "UPDATE episodes SET title = ? WHERE id = ?"
                runner.update(connection, query, title, episode.id)
                episode = get(connection, episode.id)
            }

            if (episode != null && (episode.url.isBlank() || !episode.url.equals(url, true))) {
                val runner = QueryRunner()
                val query = "UPDATE episodes SET url = ? WHERE id = ?"
                runner.update(connection, query, url, episode.id)
                episode = get(connection, episode.id)
            }

            if (episode != null && episode.duration != duration) {
                val runner = QueryRunner()
                val query = "UPDATE episodes SET duration = ? WHERE id = ?"
                runner.update(connection, query, duration, episode.id)
                episode = get(connection, episode.id)
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
                val lastNumber = animeMapper.get(
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

            get(connection, newId)
        }
    }
}