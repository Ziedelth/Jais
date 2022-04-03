/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.animes.LangType
import fr.ziedelth.jais.utils.animes.sql.data.LangTypeData
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanListHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.sql.Connection

class LangTypeMapper {
    /**
     * It takes a connection and returns a list of LangTypeData objects
     *
     * @param connection The connection to the database.
     * @return A list of LangTypeData objects.
     */
    fun get(connection: Connection?): MutableList<LangTypeData> {
        val blh = BeanListHandler(LangTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM lang_types", blh)
    }

    /**
     * Get a single LangTypeData object from the database by id
     *
     * @param connection The connection to the database.
     * @param id The id of the LangTypeData object to be retrieved.
     * @return A `LangTypeData` object.
     */
    private fun get(connection: Connection?, id: Long): LangTypeData? {
        val blh = BeanListHandler(LangTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM lang_types WHERE id = ?", blh, id).firstOrNull()
    }

    /**
     * It returns a LangTypeData object for the given name.
     *
     * @param connection The database connection to use.
     * @param name The name of the language type.
     * @return A list of LangTypeData objects.
     */
    fun get(connection: Connection?, name: String): LangTypeData? {
        val blh = BeanListHandler(LangTypeData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM lang_types WHERE `name` = ?", blh, name).firstOrNull()
    }

    /**
     * If the language type already exists, update the French translation if it's not empty. Otherwise, return the existing
     * language type
     *
     * @param connection The connection to the database.
     * @param alangType The LangType object that you want to insert.
     * @return A LangTypeData object.
     */
    fun insert(connection: Connection?, alangType: LangType): LangTypeData? {
        val langType = get(connection, alangType.name)

        return if (langType != null) {
            if (langType.fr.isEmpty() && alangType.fr.isNotEmpty()) {
                val runner = QueryRunner()
                val query = "UPDATE lang_types SET fr = ? WHERE id = ?"
                runner.update(connection, query, alangType.fr, langType.id)
                get(connection, langType.id)
            } else langType
        } else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO lang_types (name, fr) VALUES (?, ?)"
            val newId: Long = runner.insert(connection, query, sh, alangType.name, alangType.fr).toLong()
            get(connection, newId)
        }
    }
}