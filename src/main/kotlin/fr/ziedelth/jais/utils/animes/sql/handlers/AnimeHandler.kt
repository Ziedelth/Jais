/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.handlers

import fr.ziedelth.jais.utils.animes.sql.data.*
import org.apache.commons.dbutils.BasicRowProcessor
import org.apache.commons.dbutils.BeanProcessor
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanListHandler
import java.sql.Connection
import java.sql.ResultSet

class AnimeHandler(private val connection: Connection?) :
    BeanListHandler<AnimeData>(AnimeData::class.java, BasicRowProcessor(BeanProcessor(getColumnsToFieldsMap()))) {
    companion object {
        fun getColumnsToFieldsMap(): Map<String, String> {
            val map = mutableMapOf<String, String>()
            map["country_id"] = "countryId"
            map["platform_id"] = "platformId"
            map["release_date"] = "releaseDate"
            return map
        }
    }

    override fun handle(rs: ResultSet?): MutableList<AnimeData> {
        val animes = super.handle(rs)
        val runner = QueryRunner()

        val blhC = BeanListHandler(CountryData::class.java)
        val blhP = BeanListHandler(PlatformData::class.java)
        val animeGenreHandler = AnimeGenreHandler()
        val blhE = BeanListHandler(EpisodeData::class.java)
        val blhS = BeanListHandler(ScanData::class.java)

        val queryCountry = "SELECT * FROM countries WHERE id = ?"
        val queryPlatform = "SELECT * FROM platforms WHERE id = ?"
        val queryGenres = "SELECT * FROM genres WHERE anime_id = ?"
        val queryEpisodes = "SELECT * FROM episodes WHERE anime_id = ?"
        val queryScans = "SELECT * FROM scans WHERE anime_id = ?"

        animes.forEach {
            val country = runner.query(this.connection, queryCountry, blhC, it.countryId).firstOrNull()
            it.country = country
            val platform = runner.query(this.connection, queryPlatform, blhP, it.platformId).firstOrNull()
            it.platform = platform
            val genres = runner.query(this.connection, queryGenres, animeGenreHandler, it.id)
            it.genres = genres
            val episodes = runner.query(this.connection, queryEpisodes, blhE, it.id)
            it.episodes = episodes
            val scans = runner.query(this.connection, queryScans, blhS, it.id)
            it.scans = scans
        }

        return animes
    }
}