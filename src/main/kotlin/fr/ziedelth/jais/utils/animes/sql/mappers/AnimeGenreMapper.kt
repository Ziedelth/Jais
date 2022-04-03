/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.animes.sql.data.AnimeGenreData
import fr.ziedelth.jais.utils.animes.sql.handlers.AnimeGenreHandler
import org.apache.commons.dbutils.QueryRunner
import java.sql.Connection

class AnimeGenreMapper {
    /**
     * It takes a connection as a parameter, and returns a list of anime genres
     *
     * @param connection The connection to the database.
     * @return A list of AnimeGenreData objects.
     */
    fun get(connection: Connection?): MutableList<AnimeGenreData> {
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
    fun get(connection: Connection?, animeId: Long?, genreId: Long?): AnimeGenreData? {
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
    fun get(connection: Connection?, animeId: Long?): MutableList<AnimeGenreData>? {
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
    fun insert(connection: Connection?, animeId: Long?, genreId: Long?): AnimeGenreData? {
        val animeGenre = get(connection, animeId, genreId)

        return if (animeGenre != null) animeGenre
        else {
            val runner = QueryRunner()
            val query = "INSERT INTO anime_genres (anime_id, genre_id) VALUES (?, ?)"
            runner.update(connection, query, animeId, genreId)
            return get(connection, animeId, genreId)
        }
    }
}