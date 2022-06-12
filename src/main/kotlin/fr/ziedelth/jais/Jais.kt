package fr.ziedelth.jais

import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform
import fr.ziedelth.jais.platforms.CrunchyrollPlatform
import java.util.Calendar
import java.util.concurrent.Callable
import java.util.concurrent.Executors

object Jais {
    @JvmStatic
    fun main(args: Array<String>) {
        val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val platforms = arrayOf(AnimeDigitalNetworkPlatform(), CrunchyrollPlatform())

        // Create a list of dates
        val dates = List(readLine()?.toInt() ?: 1) { index ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -index)
            return@List calendar
        }

        val totalEpisodes = newFixedThreadPool.invokeAll(dates.map { date -> Callable {
            platforms.map { platform -> platform.getAllEpisodes(date) }
        } }).flatMap { it.get() }.flatten().sortedBy { it.releaseDate }

        println(totalEpisodes.size)
        totalEpisodes.forEach { println(it) }

        newFixedThreadPool.shutdown()
    }
}