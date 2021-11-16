/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.debug

object JTime {
    private val times = mutableMapOf<String, Long>()

    fun start(key: String, message: String) {
        if (!times.containsKey(key)) {
            JLogger.info("{$key} $message")
            times[key] = System.currentTimeMillis()
        }
    }

    fun end(key: String, message: String) {
        if (times.containsKey(key)) {
            val start = times[key]!!
            val end = System.currentTimeMillis()
            JLogger.config("{$key} ${message.replace("%{ms}", (end - start).toString())}")
            times.remove(key)
        }
    }
}