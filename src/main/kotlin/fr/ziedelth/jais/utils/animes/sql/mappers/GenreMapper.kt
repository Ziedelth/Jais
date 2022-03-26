/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.sql.data.GenreData
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanListHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.sql.Connection

class GenreMapper {
    /**
     * Get all the genres from the database
     *
     * @param connection The connection to the database.
     * @return A list of GenreData objects.
     */
    fun get(connection: Connection?): MutableList<GenreData> {
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
    fun get(connection: Connection?, id: Long): GenreData? {
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
    fun get(connection: Connection?, name: String?): GenreData? {
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
    fun insert(connection: Connection?, agenre: Genre): GenreData? {
        val genre = get(connection, agenre.name)

        return if (genre != null) {
            if (genre.fr.isEmpty() && agenre.fr.isNotEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE genres SET fr = ? WHERE id = ?"
                runner.update(connection, query, agenre.fr, genre.id)
                get(connection, genre.id)
            } else genre
        } else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO genres (name, fr) VALUES (?, ?)"
            val newId: Long = runner.insert(connection, query, sh, agenre.name, agenre.fr).toLong()
            get(connection, newId)
        }
    }
}