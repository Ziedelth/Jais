/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.handlers

import fr.ziedelth.jais.utils.animes.sql.data.EpisodeData
import org.apache.commons.dbutils.BasicRowProcessor
import org.apache.commons.dbutils.BeanProcessor
import org.apache.commons.dbutils.handlers.BeanListHandler

class EpisodeHandler :
    BeanListHandler<EpisodeData>(EpisodeData::class.java, BasicRowProcessor(BeanProcessor(getColumnsToFieldsMap()))) {
    companion object {
        fun getColumnsToFieldsMap(): Map<String, String> {
            val map = mutableMapOf<String, String>()
            map["platform_id"] = "platformId"
            map["anime_id"] = "animeId"
            map["episode_type_id"] = "episodeTypeId"
            map["lang_type_id"] = "langTypeId"
            map["release_date"] = "releaseDate"
            map["episode_id"] = "episodeId"
            return map
        }
    }
}