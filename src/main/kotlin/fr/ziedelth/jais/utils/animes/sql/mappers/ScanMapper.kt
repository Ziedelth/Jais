/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.animes.sql.data.ScanData
import fr.ziedelth.jais.utils.animes.sql.handlers.ScanHandler
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.sql.Connection

class ScanMapper {
    /**
     * It takes a connection and returns a list of ScanData objects
     *
     * @param connection The connection to the database.
     * @return A list of ScanData objects.
     */
    fun get(connection: Connection?): MutableList<ScanData> {
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
    fun get(connection: Connection?, id: Long?): ScanData? {
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
    fun get(connection: Connection?, platformId: Long?, animeId: Long?, number: String): ScanData? {
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
    fun insert(
        connection: Connection?,
        platformId: Long?,
        animeId: Long?,
        idEpisodeType: Long?,
        idLangType: Long?,
        releaseDate: String,
        number: String,
        url: String,
    ): ScanData? {
        val scan = get(connection, platformId, animeId, number)

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

            get(connection, newId)
        }
    }
}