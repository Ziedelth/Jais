/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.animes.sql.data.AnimeCodeData
import fr.ziedelth.jais.utils.animes.sql.data.AnimeGenreData
import fr.ziedelth.jais.utils.animes.sql.handlers.AnimeCodeHandler
import fr.ziedelth.jais.utils.animes.sql.handlers.AnimeGenreHandler
import org.apache.commons.dbutils.QueryRunner
import java.sql.Connection

class AnimeCodeMapper {
    fun get(connection: Connection?): MutableList<AnimeCodeData> {
        val ach = AnimeCodeHandler()
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM anime_codes", ach)
    }

    fun get(connection: Connection?, animeId: Long?): AnimeCodeData? {
        val ach = AnimeCodeHandler()
        val runner = QueryRunner()
        return runner.query(
            connection,
            "SELECT * FROM anime_codes WHERE anime_id = ?",
            ach,
            animeId
        ).firstOrNull()
    }

    fun get(connection: Connection?, code: String?): AnimeCodeData? {
        val ach = AnimeCodeHandler()
        val runner = QueryRunner()
        return runner.query(
            connection,
            "SELECT * FROM anime_codes WHERE code = ?",
            ach,
            code
        ).firstOrNull()
    }

    fun insert(connection: Connection?, animeId: Long?, code: String?): AnimeCodeData? {
        val animeCode = get(connection, code)

        return if (animeCode != null) animeCode
        else {
            val runner = QueryRunner()
            val query = "INSERT INTO anime_codes (anime_id, code) VALUES (?, ?)"
            runner.update(connection, query, animeId, code)
            return get(connection, code)
        }
    }
}