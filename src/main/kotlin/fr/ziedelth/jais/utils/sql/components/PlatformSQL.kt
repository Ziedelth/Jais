/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.sql.components

import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler

data class PlatformSQL(
    val id: Int,
    val name: String,
    val url: String?,
    val image: String?,
    val color: String?,
) {
    constructor(id: Int, platformHandler: PlatformHandler) : this(
        id = id,
        name = platformHandler.name,
        url = platformHandler.url,
        image = platformHandler.image,
        color = platformHandler.color.toString(16)
    )
}