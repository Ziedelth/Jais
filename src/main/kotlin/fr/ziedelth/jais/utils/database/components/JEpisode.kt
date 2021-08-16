/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.database.components

data class JEpisode(
    val id: Int,
    val timestamp: String,
    val platformId: Int,
    val numberId: Int,
    val type: String,
    val episodeId: Long,
    val title: String? = null,
    val image: String? = null,
    val url: String? = null,
    val duration: Long
) {
    override fun toString(): String {
        return "JEpisode(id=$id, timestamp='$timestamp', platformId=$platformId, numberId=$numberId, type='$type', episodeId=$episodeId, title=$title, image=$image, url=$url, duration=$duration)"
    }
}
