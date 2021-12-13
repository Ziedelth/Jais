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

        val episodeHandler = EpisodeHandler()
        val queryEpisodes = "SELECT * FROM episodes WHERE anime_id = ?"

        animes.forEach {
            val episodes = runner.query(this.connection, queryEpisodes, episodeHandler, it.id)
            it.episodes = episodes
        }

        return animes
    }
}