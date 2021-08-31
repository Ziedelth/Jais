/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.database.components

data class JAnime(
    val id: Int,
    val timestamp: String,
    val countryId: Int,
    val name: String,
    val image: String?
) {
    override fun toString(): String {
        return "JAnime(id=$id, timestamp='$timestamp', countryId=$countryId, name='$name', image=$image)"
    }
}
