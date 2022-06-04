package fr.ziedelth.jais

import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform
import java.util.Calendar
import java.util.concurrent.Callable
import java.util.concurrent.Executors

object Jais {
    @JvmStatic
    fun main(args: Array<String>) {
        val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val animeDigitalNetworkPlatform = AnimeDigitalNetworkPlatform()

        // Create a list of dates
        val dates = List(readLine()?.toInt() ?: 1) { index ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -index)
            return@List calendar
        }

        val totalEpisodes = newFixedThreadPool.invokeAll(dates.map { Callable {
            animeDigitalNetworkPlatform.getAllEpisodes(it)
        } }).flatMap { it.get() }.sortedBy { it.releaseDate }

        println(totalEpisodes.size)
        totalEpisodes.forEach { println(it) }

        newFixedThreadPool.shutdown()
    }
}