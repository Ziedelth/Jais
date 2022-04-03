/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.sql.data.EpisodeTypeData
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanListHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.sql.Connection

class EpisodeTypeMapper {
    /**
     * It returns a list of EpisodeTypeData objects.
     *
     * @param connection The connection to the database.
     * @return A list of EpisodeTypeData objects.
     */
    fun get(connection: Connection?): MutableList<EpisodeTypeData> {
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
    private fun get(connection: Connection?, id: Long): EpisodeTypeData? {
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
    fun get(connection: Connection?, name: String): EpisodeTypeData? {
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
    fun insert(connection: Connection?, aepisodeType: EpisodeType): EpisodeTypeData? {
        val episodeType = get(connection, aepisodeType.name)

        return if (episodeType != null) {
            if (episodeType.fr.isEmpty() && aepisodeType.fr.isNotEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE episode_types SET fr = ? WHERE id = ?"
                runner.update(connection, query, aepisodeType.fr, episodeType.id)
                get(connection, episodeType.id)
            } else episodeType
        } else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO episode_types (name, fr) VALUES (?, ?)"
            val newId: Long = runner.insert(connection, query, sh, aepisodeType.name, aepisodeType.fr).toLong()
            get(connection, newId)
        }
    }
}