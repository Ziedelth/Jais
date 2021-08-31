/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.database.components

data class JNumber(
    val id: Int,
    val timestamp: String,
    val seasonId: Int,
    val value: String
) {
    override fun toString(): String {
        return "JNumber(id=$id, timestamp='$timestamp', seasonId=$seasonId, value='$value')"
    }
}