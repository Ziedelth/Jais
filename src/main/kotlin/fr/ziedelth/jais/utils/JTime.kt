/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

object JTime {
    private val times = mutableMapOf<String, Long>()

    fun start(key: String, message: String) {
        if (!this.times.containsKey(key)) {
            JLogger.info("{$key} $message")
            this.times[key] = System.currentTimeMillis()
        }
    }

    fun end(key: String, message: String) {
        if (this.times.containsKey(key)) {
            val start = this.times[key]!!
            val end = System.currentTimeMillis()
            JLogger.config("{$key} ${message.replace("%{ms}", (end - start).toString())}")
            this.times.remove(key)
        }
    }
}