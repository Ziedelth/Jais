/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.database.components

data class JSeason(
    val id: Int,
    val timestamp: String,
    val animeId: Int,
    val value: String
) {
    override fun toString(): String {
        return "JSeason(id=$id, timestamp='$timestamp', animeId=$animeId, value='$value')"
    }
}