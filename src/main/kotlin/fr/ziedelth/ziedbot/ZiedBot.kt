package fr.ziedelth.ziedbot

import fr.ziedelth.ziedbot.clients.DiscordClient
import fr.ziedelth.ziedbot.threads.AnimeThread
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.ZiedLogger

object ZiedBot {
    @JvmStatic
    fun main(args: Array<String>) {
        ZiedLogger.info("Init...")
        ZiedLogger.info("Request per day: ${(60L / Const.DELAY_BETWEEN_REQUEST) * 24L}")

        Const.CLIENTS.add(DiscordClient())

        AnimeThread()
    }
}