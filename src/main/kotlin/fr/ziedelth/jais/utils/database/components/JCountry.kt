/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.database.components

data class JCountry(
    val id: Int,
    val country: String,
) {
    override fun toString(): String {
        return "JCountry(id=$id, country='$country')"
    }
}
