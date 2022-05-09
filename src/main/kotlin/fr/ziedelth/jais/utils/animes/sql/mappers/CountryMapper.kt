/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.mappers

import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.sql.data.CountryData
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanListHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import java.sql.Connection

class CountryMapper {
    /**
     * It takes a connection and returns a list of CountryData objects
     *
     * @param connection The connection to the database.
     * @return A list of CountryData objects.
     */
    fun get(connection: Connection?): MutableList<CountryData> {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries", blh)
    }

    /**
     * Get a country by id
     *
     * @param connection The connection to the database.
     * @param id The id of the country to retrieve.
     * @return Nothing.
     */
    fun get(connection: Connection?, id: Long): CountryData? {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries WHERE id = ?", blh, id).firstOrNull()
    }

    /**
     * Get a country by its tag
     *
     * @param connection The connection to the database.
     * @param tag The tag of the country you want to get.
     * @return A CountryData object.
     */
    private fun getByTag(connection: Connection?, tag: String): CountryData? {
        val blh = BeanListHandler(CountryData::class.java)
        val runner = QueryRunner()
        return runner.query(connection, "SELECT * FROM countries WHERE tag = ?", blh, tag).firstOrNull()
    }

    /**
     * Insert a country into the database
     *
     * @param connection The connection to the database.
     * @param countryHandler CountryHandler is the object that holds the data for the country.
     */
    fun insert(connection: Connection?, countryHandler: CountryHandler): CountryData? =
        insert(connection, countryHandler.tag, countryHandler.name, countryHandler.flag, countryHandler.season)

    /**
     * If the country already exists, return it. Otherwise, insert it into the database and return it
     *
     * @param connection The connection to the database.
     * @param tag The country's tag, which is a unique identifier.
     * @param name The name of the country.
     * @param flag The flag of the country.
     * @param season The season the country is in.
     * @return A CountryData object.
     */
    fun insert(connection: Connection?, tag: String, name: String, flag: String, season: String): CountryData? {
        val country = getByTag(connection, tag)

        return if (country != null) country
        else {
            val sh = ScalarHandler<Long>()
            val runner = QueryRunner()
            val query = "INSERT INTO countries (tag, name, flag, season) VALUES (?, ?, ?, ?)"
            val newId: Long = runner.insert(
                connection,
                query,
                sh,
                tag,
                name,
                flag,
                season
            ).toLong()
            get(connection, newId)
        }
    }
}