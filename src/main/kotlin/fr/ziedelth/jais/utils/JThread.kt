/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import kotlin.math.max

/* A singleton. */
object JThread {
    /* It's a counter that will be used to give a unique id to each thread. */
    private var count = 0

    /* It's a map of id to thread. */
    private val threads = mutableMapOf<Int, Thread>()

    /**
     * Create a new thread and start it
     *
     * @param action The action to be performed by the thread.
     * @param daemon If true, the thread is created as a daemon thread.
     * @param priority The priority of the thread.
     * @return The id of the thread.
     */
    fun start(action: () -> Unit, daemon: Boolean = false, priority: Int = Thread.NORM_PRIORITY): Int {
        val thread = Thread {
            action.invoke()
        }

        thread.isDaemon = daemon
        thread.priority = priority
        thread.start()

        val id = count++
        this.threads[id] = thread
        return id
    }

    /**
     * Start a thread that will run the given action every `delay` milliseconds
     *
     * @param action The action to be performed by the thread.
     * @param delay The delay in milliseconds between each execution of the action.
     * @param daemon If true, the thread will be a daemon thread.
     * @param priority The priority of the thread.
     * @return The id of the thread.
     */
    fun startExactly(
        action: () -> Unit,
        delay: Long,
        daemon: Boolean = false,
        priority: Int = Thread.NORM_PRIORITY
    ): Int {
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
        this.threads[id] = thread
        return id
    }

}