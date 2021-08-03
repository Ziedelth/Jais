package fr.ziedelth.jais.threads

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.DriverBuilder
import fr.ziedelth.jais.utils.removeAllDeprecatedConfigurations
import fr.ziedelth.jais.utils.removeAllDeprecatedReactions

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
            removeAllDeprecatedReactions()
            removeAllDeprecatedConfigurations()
            DriverBuilder.removeAllDeprecatedDrivers()
            System.gc()
        }
    }
}