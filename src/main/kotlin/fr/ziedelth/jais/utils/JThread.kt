/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import kotlin.math.max

object JThread {
    private val threads = mutableListOf<Thread>()

    fun start(action: () -> Unit, daemon: Boolean = false, priority: Int = Thread.NORM_PRIORITY) {
        val thread = Thread {
            action.invoke()
        }

        thread.isDaemon = daemon
        thread.priority = priority
        thread.start()

        this.threads.add(thread)
    }

    fun start(action: () -> Unit, delay: Long, daemon: Boolean = false, priority: Int = Thread.NORM_PRIORITY) {
        val thread = Thread {
            val currentThread = Thread.currentThread()

            while (!currentThread.isInterrupted) {
                val start = System.currentTimeMillis()
                action.invoke()
                val end = System.currentTimeMillis()
                currentThread.join(max(1, (delay - (end - start))))
            }
        }

        thread.isDaemon = daemon
        thread.priority = priority
        thread.start()

        this.threads.add(thread)
    }

    fun stopAll() {
        this.threads.forEach { it.interrupt() }
    }
}