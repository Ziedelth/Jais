/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.platforms.*
import fr.ziedelth.jais.utils.*
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.Scan
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import fr.ziedelth.jais.utils.animes.sql.JMapper
import fr.ziedelth.jais.utils.plugins.PluginManager
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass

class Jais {
    /* Creating a list of countries. */
    private val countries = mutableListOf<CountryImpl>()
    /* Creating a list of platform implementations. */
    private val platforms = mutableListOf<PlatformImpl>()
    /* Getting the current day of the year. */
    private var day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    /* Creating a list of episodes that are not in the database. */
    private val looseEpisodes = mutableListOf<Episode>()
    /* Creating a list of scans that are not in the database. */
    private val looseScans = mutableListOf<Scan>()

    /**
     * It initializes the application, loads the countries, loads the platforms, loads the plugins, and starts the daily
     * check
     */
    fun init() {
        JLogger.info("Init...")
        Notifications.init()

        JLogger.info("Adding countries...")
        this.addCountry(FranceCountry::class.java)

        JLogger.info("Adding platforms...")
        this.addPlatform(AnimeDigitalNetworkPlatform::class.java)
        this.addPlatform(CrunchyrollPlatform::class.java)
//        this.addPlatform(NetflixPlatform::class.java)
        this.addPlatform(ScantradPlatform::class.java)
        this.addPlatform(WakanimPlatform::class.java)

        JLogger.info("Setup all plugins...")
        PluginManager.loadAll()

        JLogger.info("Starting...")

        JThread.startExactly({
            this.resetDaily()
            this.checkEpisodesAndScans()
        }, delay = 2 * 60 * 1000L, priority = Thread.MAX_PRIORITY)
    }

    /**
     * It saves the number of followers for each plugin in a JSON file
     */
    private fun saveAnalytics() {
        val gson = Gson()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val file = FileImpl.getFile("analytics.json")

        if (!file.exists()) {
            file.createNewFile()
            Files.write(file.toPath(), gson.toJson(JsonObject()).toByteArray())
        }

        val arraySaved = gson.fromJson(FileReader(file), JsonObject::class.java) ?: JsonObject()
        val currentDay = JsonObject()

        PluginManager.plugins?.forEach {
            val pluginId = it.wrapper.pluginId
            val followers = it.getFollowers()
            currentDay.addProperty(pluginId, followers)
        }

        arraySaved.add(SimpleDateFormat("dd/MM/yyyy").format(calendar.time), currentDay)
        Files.write(file.toPath(), gson.toJson(arraySaved).toByteArray())
    }

    /**
     * It checks if the day has changed, and if it has, it resets the daily data
     */
    private fun resetDaily() {
        val checkDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (checkDay != day) {
            JLogger.info("Resetting checked episodes...")
            day = checkDay

            Notifications.clear()
            this.saveAnalytics()
            PluginManager.plugins?.forEach { it.reset() }
            this.platforms.forEach { it.platform.reset() }
        }
    }

    /**
     * It checks the episodes and scans of all the platforms and saves them in the database
     *
     * @param calendar Calendar = Calendar.getInstance()
     */
    private fun checkEpisodesAndScans(calendar: Calendar = Calendar.getInstance()) {
        Impl.tryCatch("Failed to fetch episodes") {
            JLogger.info("Check if has Internet...")
            if (Impl.hasInternet()) {
                val gson = Gson()
                val (episodesFile, episodesSaved) = getEpisodeFile(gson)
                val (scansFile, scansSaved) = getScanFile(gson)
                val (newsFile, newsSaved) = getNewsFile(gson)
                val connection = JMapper.getConnection()
                val isConnected = connection != null && !connection.isClosed
                val startTime = System.currentTimeMillis()

                if (isConnected) {
                    if (this.looseEpisodes.isNotEmpty()) {
                        val list = mutableListOf<Episode>()
                        JLogger.info("Retry insertion of loose episode...")

                        this.looseEpisodes.forEach { episode ->
                            val episodeData = JMapper.insertEpisode(connection, episode)
                            val ifExists = JMapper.getEpisode(connection, episodeData?.id) != null

                            if (ifExists) {
                                JLogger.info("Episode has been correctly inserted or updated in the database!")
                                list.add(episode)
                            } else {
                                JLogger.warning("Episode has occurred a problem when insertion, retry next time...")
                            }
                        }

                        this.looseEpisodes.removeAll(list)
                    }

                    if (this.looseScans.isNotEmpty()) {
                        val list = mutableListOf<Scan>()
                        JLogger.info("Retry insertion of loose scan...")

                        this.looseScans.forEach { scan ->
                            val scanData = JMapper.insertScan(connection, scan)
                            val ifExists = JMapper.getScan(connection, scanData?.id) != null

                            if (ifExists) {
                                JLogger.info("Scan has been correctly inserted or updated in the database!")
                                list.add(scan)
                            } else {
                                JLogger.warning("Scan has occurred a problem when insertion, retry next time...")
                            }
                        }

                        this.looseScans.removeAll(list)
                    }
                }

                this.platforms.forEach { platformImpl ->
                    JLogger.info("[${platformImpl.platformHandler.name}] Fetching episodes...")
                    val episodes = platformImpl.platform.checkEpisodes(calendar).sortedWith(compareBy(Episode::anime, Episode::releaseDate, Episode::season, Episode::number, Episode::episodeType, Episode::langType))
                    JLogger.info("[${platformImpl.platformHandler.name}] Fetching scans...")
                    val scans = platformImpl.platform.checkScans(calendar).sortedWith(compareBy(Scan::anime, Scan::releaseDate, Scan::number, Scan::episodeType, Scan::langType))
                    JLogger.info("[${platformImpl.platformHandler.name}] Fetching news...")
                    val news = platformImpl.platform.checkNews(calendar)
                    JLogger.config("[${platformImpl.platformHandler.name}] All fetched! Episodes length: ${episodes.size} - Scans length: ${scans.size} - News length: ${news.size}")

                    if (episodes.isNotEmpty()) {
                        JLogger.info("[${platformImpl.platformHandler.name}] Insert all episodes...")

                        Impl.tryCatch("[${platformImpl.platformHandler.name}] Cannot insert episodes!") {
                            episodes.sortedBy { it.releaseDate }.forEach { episode ->
                                if (!episodesSaved.contains(episode.episodeId)) {
                                    episodesSaved.add(episode.episodeId)
                                    Files.write(episodesFile.toPath(), gson.toJson(episodesSaved).toByteArray())

                                    Notifications.add(episode.anime)

                                    PluginManager.plugins?.forEach {
                                        Impl.tryCatch("Can not send episode for ${it.wrapper.pluginId} plugin") {
                                            it.newEpisode(episode)
                                        }
                                    }
                                }

                                if (isConnected) {
                                    val episodeData = JMapper.insertEpisode(connection, episode)
                                    val ifExists = JMapper.getEpisode(connection, episodeData?.id) != null

                                    if (ifExists) {
                                        JLogger.info("Episode has been correctly inserted or updated in the database!")
                                    } else {
                                        JLogger.warning("Episode has occurred a problem when insertion, retry next time...")
                                        this.looseEpisodes.add(episode)
                                    }
                                }
                            }
                        }

                        JLogger.info("[${platformImpl.platformHandler.name}] All episodes has been inserted!")
                    }

                    if (scans.isNotEmpty()) {
                        JLogger.info("[${platformImpl.platformHandler.name}] Insert all scans...")

                        Impl.tryCatch("[${platformImpl.platformHandler.name}] Cannot insert scans!") {
                            scans.sortedBy { it.releaseDate }.forEach { scan ->
                                if (!scansSaved.contains(scan.hashCode())) {
                                    scansSaved.add(scan.hashCode())
                                    Files.write(scansFile.toPath(), gson.toJson(scansSaved).toByteArray())

                                    Notifications.add(scan.anime)

                                    PluginManager.plugins?.forEach {
                                        Impl.tryCatch("Can not send scan for ${it.wrapper.pluginId} plugin") {
                                            it.newScan(scan)
                                        }
                                    }
                                }

                                if (isConnected) {
                                    val scanData = JMapper.insertScan(connection, scan)
                                    val ifExists = JMapper.getScan(connection, scanData?.id) != null

                                    if (ifExists) {
                                        JLogger.info("Scan has been correctly inserted or updated in the database!")
                                    } else {
                                        JLogger.warning("Scan has occurred a problem when insertion, retry next time...")
                                        this.looseScans.add(scan)
                                    }
                                }
                            }
                        }

                        JLogger.info("[${platformImpl.platformHandler.name}] All scans has been inserted!")
                    }

                    if (news.isNotEmpty()) {
                        JLogger.info("[${platformImpl.platformHandler.name}] Insert all news...")

                        Impl.tryCatch("[${platformImpl.platformHandler.name}] Cannot insert news!") {
                            news.sortedBy { it.releaseDate }.forEach { news ->
                                if (!newsSaved.contains(news.hashCode())) {
                                    newsSaved.add(news.hashCode())
                                    Files.write(newsFile.toPath(), gson.toJson(newsSaved).toByteArray())

                                    PluginManager.plugins?.forEach {
                                        Impl.tryCatch("Can not send news for ${it.wrapper.pluginId} plugin") {
                                            it.newNews(news)
                                        }
                                    }
                                }
                            }
                        }

                        JLogger.info("[${platformImpl.platformHandler.name}] All news has been inserted!")
                    }
                }

                Notifications.send()
                val endTime = System.currentTimeMillis()
                JLogger.info("All platforms has been checked in ${endTime - startTime}ms!")

                connection?.close()
            } else {
                JLogger.warning("No internet")
            }
        }
    }

    /**
     * It creates a file called episodes.json if it doesn't exist, and if it does exist, it reads the file and returns the
     * contents as a list of strings
     *
     * @param gson Gson
     * @return A pair of a file and a mutable list of strings.
     */
    private fun getEpisodeFile(gson: Gson): Pair<File, MutableList<String>> {
        val file = FileImpl.getFile("episodes.json")

        if (!file.exists()) {
            file.createNewFile()
            Files.write(file.toPath(), gson.toJson(JsonArray()).toByteArray())
        }

        val episodesSaved =
            (gson.fromJson(FileReader(file), Array<String>::class.java) ?: emptyArray()).toMutableList()
        return Pair(file, episodesSaved)
    }

    /**
     * It creates a file called "scans.json" if it doesn't exist, and if it does exist, it reads the file and converts the
     * contents to a list of integers
     *
     * @param gson Gson
     * @return A pair of a file and a mutable list of integers.
     */
    private fun getScanFile(gson: Gson): Pair<File, MutableList<Int>> {
        val file = FileImpl.getFile("scans.json")

        if (!file.exists()) {
            file.createNewFile()
            Files.write(file.toPath(), gson.toJson(JsonArray()).toByteArray())
        }

        val scansSaved =
            (gson.fromJson(FileReader(file), Array<Int>::class.java) ?: emptyArray()).toMutableList()
        return Pair(file, scansSaved)
    }

    /**
     * If the file doesn't exist, create it and write an empty JSON array to it
     *
     * @param gson Gson
     * @return A pair of a file and a mutable list of integers.
     */
    private fun getNewsFile(gson: Gson): Pair<File, MutableList<Int>> {
        val file = FileImpl.getFile("news.json")

        if (!file.exists()) {
            file.createNewFile()
            Files.write(file.toPath(), gson.toJson(JsonArray()).toByteArray())
        }

        val newsSaved =
            (gson.fromJson(FileReader(file), Array<Int>::class.java) ?: emptyArray()).toMutableList()
        return Pair(file, newsSaved)
    }

    /**
     * If the country is not already in the list of countries, and the country is annotated with the CountryHandler
     * annotation, add the country to the list of countries
     *
     * @param country The class of the country to add.
     * @return A boolean value.
     */
    fun addCountry(country: Class<out Country>): Boolean {
        return if (this.countries.none { it.country::class.java == country } && country.isAnnotationPresent(
                CountryHandler::class.java
            )) {
            this.countries.add(
                CountryImpl(
                    countryHandler = country.getAnnotation(CountryHandler::class.java),
                    country = country.getConstructor().newInstance()
                )
            )

            true
        } else false
    }

    /**
     * If the platform is not already in the list of platforms, and the platform is annotated with the PlatformHandler
     * annotation, then add the platform to the list of platforms
     *
     * @param platform The platform class that you want to add.
     * @return A boolean value.
     */
    fun addPlatform(platform: Class<out Platform>): Boolean {
        return if (this.platforms.none { it.platform::class.java == platform } && platform.isAnnotationPresent(
                PlatformHandler::class.java
            )) {
            this.platforms.add(
                PlatformImpl(
                    platformHandler = platform.getAnnotation(PlatformHandler::class.java),
                    platform = platform.getConstructor(Jais::class.java).newInstance(this)
                )
            )

            true
        } else false
    }

    /**
     * If the country is not null, return the first country in the list of countries that has the same class as the
     * country. Otherwise, return null
     *
     * @param country The country to get information about.
     * @return The first country that has the same class as the country parameter.
     */
    fun getCountryInformation(country: Country?): CountryImpl? {
        return if (country != null) this.countries.firstOrNull { it.country::class.java == country::class.java } else null
    }

    /**
     * Get the country information for the given country
     *
     * @param country The country class that you want to get information about.
     * @return The country information for the given country class.
     */
    private fun getCountryInformation(country: KClass<out Country>?): CountryImpl? {
        return this.countries.firstOrNull { it.country::class.java == country?.java }
    }

    /**
     * If the platform is not null, return the platform from the list of platforms. Otherwise, return null
     *
     * @param platform The platform to get information for.
     * @return The platform information for the given platform.
     */
    fun getPlatformInformation(platform: Platform?): PlatformImpl? {
        return if (platform != null) this.platforms.firstOrNull { it.platform::class.java == platform::class.java } else null
    }

    /**
     * If the platform is not null, return the countries of the platform. Otherwise, return an empty array
     *
     * @param platform Platform?
     * @return An array of countries.
     */
    fun getAllowedCountries(platform: Platform?): Array<Country> {
        return this.getPlatformInformation(platform)?.platformHandler?.countries?.mapNotNull { platformCountry ->
            this.getCountryInformation(
                platformCountry
            )
        }?.map { it.country }?.toTypedArray() ?: arrayOf()
    }
}