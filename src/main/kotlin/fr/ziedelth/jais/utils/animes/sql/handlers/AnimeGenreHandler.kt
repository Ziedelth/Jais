/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.handlers

import fr.ziedelth.jais.utils.animes.sql.data.AnimeGenreData
import org.apache.commons.dbutils.BasicRowProcessor
import org.apache.commons.dbutils.BeanProcessor
import org.apache.commons.dbutils.handlers.BeanListHandler

class AnimeGenreHandler : BeanListHandler<AnimeGenreData>(
    AnimeGenreData::class.java,
    BasicRowProcessor(BeanProcessor(getColumnsToFieldsMap()))
) {
    companion object {
        fun getColumnsToFieldsMap(): Map<String, String> {
            val map = mutableMapOf<String, String>()
            map["anime_id"] = "animeId"
            return map
        }
    }
}