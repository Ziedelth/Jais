package fr.ziedelth.jais.threads

import fr.ziedelth.jais.utils.Const

class UpdateThread : Runnable {
    private val thread = Thread(this, "UpdateThread")

    init {
        this.thread.isDaemon = true
        this.thread.start()
    }

    override fun run() {
        while (!this.thread.isInterrupted) {
            this.thread.join(3600000L)
            Const.CLIENTS.forEach { it.update() }
            System.gc()
        }
    }
}