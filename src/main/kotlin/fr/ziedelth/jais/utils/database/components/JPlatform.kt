/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.database.components

data class JPlatform(
    val id: Int,
    val name: String,
    val url: String,
    val image: String,
    val color: String
) {
    override fun toString(): String {
        return "JPlatform(id=$id, name='$name', url='$url', image='$image', color='$color')"
    }
}
