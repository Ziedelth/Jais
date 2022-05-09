/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.sql.data.PlatformData
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanListHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.sql.Connection

class PlatformMapper {
    fun get(connection: Connection?): MutableList<PlatformData> {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms", blh)
    }

    fun get(connection: Connection?, id: Long): PlatformData? {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms WHERE id = ?", blh, id).firstOrNull()
    }

    fun get(connection: Connection?, name: String?): PlatformData? {
        val blh = BeanListHandler(PlatformData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM platforms WHERE name = ?", blh, name).firstOrNull()
    }

    fun insert(connection: Connection?, platformHandler: PlatformHandler): PlatformData? = insert(
        connection,
        platformHandler.name,
        platformHandler.url,
        platformHandler.image,
        platformHandler.color
    )

    /**
     * If the platform already exists, return it. Otherwise, insert it into the database and return it
     *
     * @param connection The connection to the database.
     * @param name The name of the platform.
     * @param url The URL of the platform.
     * @param image The image of the platform.
     * @param color The color of the platform.
     * @return A PlatformData object.
     */
    fun insert(connection: Connection?, name: String, url: String, image: String, color: Int): PlatformData? {
        val platform = get(connection, name)

        return if (platform != null) platform
        else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO platforms (name, url, image, color) VALUES (?, ?, ?, ?)"
            val newId: Long = runner.insert(
                connection,
                query,
                sh,
                name,
                url,
                image,
                color
            ).toLong()
            get(connection, newId)
        }
    }
}