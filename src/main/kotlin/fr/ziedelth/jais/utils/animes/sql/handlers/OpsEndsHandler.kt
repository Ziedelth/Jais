/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.sql.handlers

import fr.ziedelth.jais.utils.animes.sql.data.OpsEndsData
import org.apache.commons.dbutils.BasicRowProcessor
import org.apache.commons.dbutils.BeanProcessor
import org.apache.commons.dbutils.handlers.BeanListHandler

class OpsEndsHandler :
    BeanListHandler<OpsEndsData>(OpsEndsData::class.java, BasicRowProcessor(BeanProcessor(getColumnsToFieldsMap()))) {
    companion object {
        fun getColumnsToFieldsMap(): Map<String, String> {
            val map = mutableMapOf<String, String>()
            map["anime_id"] = "animeId"
            map["ops_ends_type_id"] = "opsEndsTypeId"
            return map
        }
    }
}