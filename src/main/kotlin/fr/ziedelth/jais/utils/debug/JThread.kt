/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.debug

import kotlin.math.max

object JThread {
    private var count = 0
    private val threads = mutableMapOf<Int, Thread>()

    fun start(action: () -> Unit, daemon: Boolean = false, priority: Int = Thread.NORM_PRIORITY): Int {
        val thread = Thread {
            action.invoke()
        }

        thread.isDaemon = daemon
        thread.priority = priority
        thread.start()

        val id = count++
        threads[id] = thread
        return id
    }

    fun start(action: () -> Unit, delay: Long, daemon: Boolean = false, priority: Int = Thread.NORM_PRIORITY): Int {
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

        val id = count++
        threads[id] = thread
        return id
    }

    fun stop(id: Int) {
        threads[id]?.interrupt()
        threads.remove(id)
    }

    fun stopAll() {
        threads.forEach { it.value.interrupt() }
    }
}