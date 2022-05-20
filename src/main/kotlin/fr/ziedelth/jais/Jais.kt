package fr.ziedelth.jais

import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform

object Jais {
    @JvmStatic
    fun main(args: Array<String>) {
        val animeDigitalNetworkPlatform = AnimeDigitalNetworkPlatform()

        try {
            animeDigitalNetworkPlatform.getAllEpisodes()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}