/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import com.google.gson.Gson
import com.google.gson.JsonArray
import fr.ziedelth.jais.commands.ExitCommand
import fr.ziedelth.jais.commands.SendCommand
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.platforms.*
import fr.ziedelth.jais.utils.*
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.Scan
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.sql.JMapper
import fr.ziedelth.jais.utils.commands.Command
import fr.ziedelth.jais.utils.commands.CommandHandler
import fr.ziedelth.jais.utils.plugins.PluginManager
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import kotlin.reflect.KClass

class Jais {
    private val countries = mutableListOf<Pair<CountryHandler, Country>>()
    private val platforms = mutableListOf<Pair<PlatformHandler, Platform>>()
    private val commands = mutableListOf<Pair<CommandHandler, Command>>()
    private var day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    private val looseEpisodes = mutableListOf<Episode>()
    private val looseScans = mutableListOf<Scan>()

    fun init() {
        JLogger.info("Init...")
        Notifications.init()

        JLogger.info("Adding countries...")
        this.addCountry(FranceCountry::class.java)

        JLogger.info("Adding platforms...")
        this.addPlatform(AnimeDigitalNetworkPlatform::class.java)
        this.addPlatform(CrunchyrollPlatform::class.java)
        this.addPlatform(NetflixPlatform::class.java)
        this.addPlatform(ScantradPlatform::class.java)
        this.addPlatform(WakanimPlatform::class.java)
        this.addPlatform(JapscanPlatform::class.java)

        JLogger.info("Adding commands...")
        this.addCommand(ExitCommand::class.java)
        this.addCommand(SendCommand::class.java)

        JLogger.info("Setup all plugins...")
        PluginManager.loadAll()

        JLogger.info("Starting...")

        JThread.startExactly({
            this.resetDaily()
            this.checkEpisodesAndScans()
        }, delay = 5 * 60 * 1000L, priority = Thread.MAX_PRIORITY)

        JThread.start({
            val scanner = Scanner(System.`in`)

            while (true) {
                val line = scanner.nextLine()
                val allArgs = line.split(" ")

                val command = allArgs[0]
                val args = allArgs.subList(min(1, allArgs.size), allArgs.size)
                this.commands.firstOrNull { it.first.command == command }?.second?.onCommand(args)
            }
        })
    }

    private fun saveAnalytics() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val file = FileImpl.getFile("analytics.csv")

        if (!file.exists()) {
            file.createNewFile()
            Files.write(file.toPath(), "date;plugin;followers\n".toByteArray())
        }

        val builder = StringBuilder(Files.readString(file.toPath()))
        val currentDay = SimpleDateFormat("dd/MM/yyyy").format(calendar.time)

        PluginManager.plugins?.forEach {
            val pluginId = it.wrapper.pluginId

            val followers = try {
                it.getFollowers()
            } catch (e: Exception) {
                -1
            }

            builder.append("$currentDay;$pluginId;$followers\n")
        }

        Files.write(file.toPath(), builder.toString().toByteArray())
    }

    private fun resetDaily() {
        val checkDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (checkDay == day)
            return

        JLogger.info("Resetting checked episodes...")
        day = checkDay

        Notifications.clear()
        this.saveAnalytics()
        PluginManager.plugins?.forEach { it.reset() }
        this.platforms.forEach { it.second.reset() }
    }

    private fun insertFailed(connection: Connection?) {
        if (this.looseEpisodes.isNotEmpty()) {
            val list = mutableSetOf<Episode>()
            JLogger.info("Retry insertion of loose episode...")

            this.looseEpisodes.forEach { episode ->
                val episodeData = JMapper.insertEpisode(connection, episode)
                val ifExists = JMapper.episodeMapper.get(connection, episodeData?.id) != null

                if (!ifExists) {
                    JLogger.warning("Episode has occurred a problem when insertion, retry next time...")
                    return
                }

                JLogger.info("Episode has been correctly inserted or updated in the database!")
                list.add(episode)
            }

            this.looseEpisodes.removeAll(list)
            connection?.commit()
        }

        if (this.looseScans.isNotEmpty()) {
            val list = mutableSetOf<Scan>()
            JLogger.info("Retry insertion of loose scan...")

            this.looseScans.forEach { scan ->
                val scanData = JMapper.insertScan(connection, scan)
                val ifExists = JMapper.scanMapper.get(connection, scanData?.id) != null

                if (!ifExists) {
                    JLogger.warning("Scan has occurred a problem when insertion, retry next time...")
                    return
                }

                JLogger.info("Scan has been correctly inserted or updated in the database!")
                list.add(scan)
            }

            this.looseScans.removeAll(list)
            connection?.commit()
        }
    }

    private fun sortScans(
        platform: Platform,
        calendar: Calendar
    ) = platform.checkScans(calendar).sortedWith(
        compareBy(
            Scan::anime,
            Scan::releaseDate,
            Scan::number,
            Scan::episodeType,
            Scan::langType
        )
    )

    private fun sortEpisode(
        platform: Platform,
        calendar: Calendar
    ) = platform.checkEpisodes(calendar).sortedWith(
        compareBy(
            Episode::anime,
            Episode::releaseDate,
            Episode::season,
            Episode::number,
            Episode::episodeType,
            Episode::langType
        )
    )

    private fun createFile(file: File, gson: Gson) {
        if (!file.exists()) {
            file.createNewFile()
            Files.write(file.toPath(), gson.toJson(JsonArray()).toByteArray())
        }
    }

    private fun getEpisodeFile(gson: Gson): Pair<File, MutableList<String>> {
        val file = FileImpl.getFile("episodes.json")
        this.createFile(file, gson)
        val episodesSaved = (gson.fromJson(FileReader(file), Array<String>::class.java) ?: emptyArray()).toMutableList()
        return file to episodesSaved
    }

    private fun getScanFile(gson: Gson): Pair<File, MutableList<String>> {
        val file = FileImpl.getFile("scans.json")
        this.createFile(file, gson)
        val scansSaved =
            (gson.fromJson(FileReader(file), Array<String>::class.java) ?: emptyArray()).toMutableList()
        return file to scansSaved
    }

    private fun getNewsFile(gson: Gson): Pair<File, MutableList<String>> {
        val file = FileImpl.getFile("news.json")
        this.createFile(file, gson)
        val newsSaved =
            (gson.fromJson(FileReader(file), Array<String>::class.java) ?: emptyArray()).toMutableList()
        return file to newsSaved
    }

    private fun checkEpisodesAndScans(calendar: Calendar = Calendar.getInstance()) {
        Impl.tryCatch("Failed to fetch episodes") {
            JLogger.info("Check if has Internet...")

            if (!Impl.hasInternet()) {
                JLogger.warning("No Internet connection!")
                return@tryCatch
            }

            val gson = Gson()
            val (episodesFile, episodesSaved) = getEpisodeFile(gson)
            val (scansFile, scansSaved) = getScanFile(gson)
            val (newsFile, newsSaved) = getNewsFile(gson)
            val connection = JMapper.getConnection()
            connection?.autoCommit = false
            val startTime = System.currentTimeMillis()

            this.insertFailed(connection)

            this.platforms.forEach { (platformHandler, platform) ->
                JLogger.info("[${platformHandler.name}] Fetching episodes...")
                val episodes = sortEpisode(platform, calendar)
                JLogger.info("[${platformHandler.name}] Fetching scans...")
                val scans = sortScans(platform, calendar)
                JLogger.info("[${platformHandler.name}] Fetching news...")
                val news = platform.checkNews(calendar)
                JLogger.config("[${platformHandler.name}] All fetched! Episodes length: ${episodes.size} - Scans length: ${scans.size} - News length: ${news.size}")

                if (episodes.isNotEmpty()) {
                    JLogger.info("[${platformHandler.name}] Insert all episodes...")

                    Impl.tryCatch("[${platformHandler.name}] Cannot insert episodes!") {
                        episodes.forEachIndexed { _, episode ->
                            val episodeData = JMapper.insertEpisode(connection, episode)
                            val ifExistsAfterInsertion = JMapper.episodeMapper.get(connection, episodeData?.id) != null

                            if (!ifExistsAfterInsertion) {
                                JLogger.warning("Episode has occurred a problem when insertion, retry next time...")
                                this.looseEpisodes.add(episode)
                                return@forEachIndexed
                            }

                            if (!episodesSaved.contains(episode.episodeId)) {
                                episodesSaved.add(episode.episodeId)
                                Files.write(episodesFile.toPath(), gson.toJson(episodesSaved).toByteArray())
                                PluginManager.sendEpisode(episode)
                                episodeData?.let { Notifications.add(Anime(it.animeId, episode.anime)) }
                            }
                        }
                    }

                    connection?.commit()
                    JLogger.info("[${platformHandler.name}] All episodes has been inserted!")
                }

                if (scans.isNotEmpty()) {
                    JLogger.info("[${platformHandler.name}] Insert all scans...")

                    Impl.tryCatch("[${platformHandler.name}] Cannot insert scans!") {
                        scans.forEachIndexed { _, scan ->
                            JLogger.config(scan.toString())
                            val scanData = JMapper.insertScan(connection, scan)
                            val ifExistsAfterInsertion = JMapper.scanMapper.get(connection, scanData?.id) != null

                            if (!ifExistsAfterInsertion) {
                                JLogger.warning("Scan has occurred a problem when insertion, retry next time...")
                                this.looseScans.add(scan)
                                return@forEachIndexed
                            }

                            if (!scansSaved.contains(scan.scanId)) {
                                scansSaved.add(scan.scanId)
                                Files.write(scansFile.toPath(), gson.toJson(scansSaved).toByteArray())
                                PluginManager.sendScan(scan)
                                scanData?.let { Notifications.add(Anime(it.animeId, scan.anime)) }
                            }
                        }
                    }

                    connection?.commit()
                    JLogger.info("[${platformHandler.name}] All scans has been inserted!")
                }

                if (news.isNotEmpty()) {
                    JLogger.info("[${platformHandler.name}] Insert all news...")

                    Impl.tryCatch("[${platformHandler.name}] Cannot insert news!") {
                        news.forEach { news ->
                            if (!newsSaved.contains(news.newsId)) {
                                newsSaved.add(news.newsId)
                                Files.write(newsFile.toPath(), gson.toJson(newsSaved).toByteArray())
                                PluginManager.sendNews(news)
                            }
                        }
                    }

                    JLogger.info("[${platformHandler.name}] All news has been inserted!")
                }
            }

            Notifications.send()
            val endTime = System.currentTimeMillis()
            JLogger.info("All platforms has been checked in ${endTime - startTime}ms!")

            connection?.close()
        }
    }

    private fun addCountry(country: Class<out Country>) {
        if (this.countries.none { it.second::class.java == country } && country.isAnnotationPresent(
                CountryHandler::class.java
            )) {
            this.countries.add(
                country.getAnnotation(CountryHandler::class.java) to country.getConstructor().newInstance()
            )
        }
    }

    private fun getCountryInformation(country: KClass<out Country>?) =
        this.countries.firstOrNull { it.second::class.java == country?.java }

    private fun addPlatform(platform: Class<out Platform>) {
        if (this.platforms.none { it.second::class.java == platform } && platform.isAnnotationPresent(
                PlatformHandler::class.java
            )) {
            this.platforms.add(
                platform.getAnnotation(PlatformHandler::class.java) to platform.getConstructor(Jais::class.java)
                    .newInstance(this)
            )
        }
    }

    fun getPlatformInformation(platform: Platform?) =
        if (platform != null) this.platforms.firstOrNull { it.second::class.java == platform::class.java } else null

    fun getAllowedCountries(platform: Platform?) =
        this.getPlatformInformation(platform)?.first?.countries?.mapNotNull { platformCountry ->
            this.getCountryInformation(platformCountry)
        }?.toTypedArray() ?: emptyArray()

    private fun addCommand(command: Class<out Command>) {
        if (this.commands.none { it.second::class.java == command } && command.isAnnotationPresent(
                CommandHandler::class.java
            )) {
            this.commands.add(
                command.getAnnotation(CommandHandler::class.java) to command.getConstructor().newInstance()
            )
        }
    }
}