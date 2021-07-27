package fr.ziedelth.jais

import fr.ziedelth.jais.clients.DiscordClient
import fr.ziedelth.jais.threads.AnimeThread
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger

object Jais {
    var isRunning = true

    @JvmStatic
    fun main(args: Array<String>) {
        JLogger.info("Init...")
        JLogger.info("Request per day: ${(60L / Const.DELAY_BETWEEN_REQUEST) * 24L}")

        Const.CLIENTS.addAll(arrayOf(DiscordClient() /* TwitterClient() */))

        AnimeThread()

        while (isRunning) Thread.sleep(25)
    }
}