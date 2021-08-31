/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

object JTime {
    private val map: MutableMap<String, Long> = mutableMapOf()

    fun start(id: String, message: String? = null) {
        this.map[id] = System.currentTimeMillis()
        if (!message.isNullOrEmpty()) JLogger.info("[S/$id] $message")
    }

    fun end(id: String, message: String? = null, subtracted: Long = 0): Long {
        val delta = System.currentTimeMillis() - this.map.getOrDefault(id, System.currentTimeMillis())
        this.map.remove(id)
        if (!message.isNullOrEmpty()) JLogger.info(
            "[E/$id] ${
                message
                    .replace("%{ms}", delta.toString())
                    .replace("%{s}", (delta / 1000.0).toString())
                    .replace("%{dms}", (subtracted - delta).toString())
                    .replace("%{ds}", ((subtracted - delta) / 1000.0).toString())
            }"
        )
        return delta
    }
}