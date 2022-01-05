/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.max

object JThread {
    private var count = 0
    private val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val threads = mutableMapOf<Int, Thread>()

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
        this.threads[id] = thread
        return id
    }

    fun startMultiThreads(actions: Iterable<() -> Unit>): MutableList<Future<Unit>> =
        this.pool.invokeAll(actions.map { Callable(it) })

    fun stop(id: Int) {
        this.threads[id]?.interrupt()
        this.threads.remove(id)
    }

    fun stopAll() {
        this.threads.forEach { it.value.interrupt() }
        this.pool.shutdown()
    }
}