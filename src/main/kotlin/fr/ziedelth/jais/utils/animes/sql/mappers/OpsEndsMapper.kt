/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.animes.sql.data.OpsEndsData
import fr.ziedelth.jais.utils.animes.sql.data.OpsEndsTypeData
import fr.ziedelth.jais.utils.animes.sql.handlers.OpsEndsHandler
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanListHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.sql.Connection

class OpsEndsMapper {
    /**
     * It returns a list of OpsEndsTypeData objects.
     *
     * @param connection The connection to the database.
     * @return A list of OpsEndsTypeData objects.
     */
    fun getTypes(connection: Connection?): MutableList<OpsEndsTypeData> {
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
    fun get(connection: Connection?): MutableList<OpsEndsData> {
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
    fun insert(
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
}