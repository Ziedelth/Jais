package fr.ziedelth.jais

import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform
import fr.ziedelth.jais.platforms.CrunchyrollPlatform
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

object Jais {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Initializing...")
        println("Setting up thread pool...")
        val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        println("Setting up platforms...")
        val platforms = arrayOf(AnimeDigitalNetworkPlatform(), CrunchyrollPlatform())
        println("Setting up episodes...")
        val totalEpisodes = newFixedThreadPool.invokeAll(platforms.map { platform -> Callable {
            platform.getAllEpisodes(Calendar.getInstance())
        } }).flatMap { it.get() }

        totalEpisodes.forEach {
            println("${it.title} - ${it.anime.name} - ${it.releaseDate}")
            ZonedDateTime.parse(it.releaseDate).toInstant()
        }

        println(totalEpisodes.size)
        totalEpisodes.forEach { println(it) }
        newFixedThreadPool.shutdown()
    }
}