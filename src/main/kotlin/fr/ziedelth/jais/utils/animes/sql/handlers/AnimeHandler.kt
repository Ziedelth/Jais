/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.handlers

import fr.ziedelth.jais.utils.animes.sql.data.AnimeData
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
            map["release_date"] = "releaseDate"
            return map
        }
    }

    override fun handle(rs: ResultSet?): MutableList<AnimeData> {
        val animes = super.handle(rs)
        val runner = QueryRunner()

        val genreHandler = AnimeGenreHandler()
        val episodeHandler = EpisodeHandler()
        val scanHandler = ScanHandler()

        animes.forEach {
            it.genres =
                runner.query(this.connection, "SELECT * FROM anime_genres WHERE anime_id = ?", genreHandler, it.id)
            it.episodes =
                runner.query(this.connection, "SELECT * FROM episodes WHERE anime_id = ?", episodeHandler, it.id)
            it.scans = runner.query(this.connection, "SELECT * FROM scans WHERE anime_id = ?", scanHandler, it.id)
        }

        return animes
    }
}