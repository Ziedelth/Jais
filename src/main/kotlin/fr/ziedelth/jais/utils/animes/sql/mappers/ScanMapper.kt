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
    fun get(connection: Connection?): MutableList<ScanData> {
        val scanHandler = ScanHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM scans", scanHandler)
    }

    fun get(connection: Connection?, id: Long?): ScanData? {
        val scanHandler = ScanHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM scans WHERE id = ?", scanHandler, id).firstOrNull()
    }

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

    fun insert(
        connection: Connection?,
        platformId: Long?,
        animeId: Long?,
        episodeTypeId: Long?,
        langTypeId: Long?,
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
                "INSERT INTO scans (platform_id, anime_id, episode_type_id, lang_type_id, release_date, number, url) VALUES (?, ?, ?, ?, ?, ?, ?)"
            val newId: Long = runner.insert(
                connection,
                query,
                sh,
                platformId,
                animeId,
                episodeTypeId,
                langTypeId,
                releaseDate,
                number,
                url
            )

            get(connection, newId)
        }
    }
}