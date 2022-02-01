/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
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
import java.io.FileInputStream
import java.io.FileReader
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass

class Jais {
    private val countries = mutableListOf<CountryImpl>()
    private val platforms = mutableListOf<PlatformImpl>()
    private var day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    private val looseEpisodes = mutableListOf<Episode>()
    private val looseScans = mutableListOf<Scan>()

    fun init() {
        JLogger.info("Init...")

        JLogger.info("Adding countries...")
        this.addCountry(FranceCountry::class.java)

        JLogger.info("Adding platforms...")
        this.addPlatform(AnimeDigitalNetworkPlatform::class.java)
        this.addPlatform(CrunchyrollPlatform::class.java)
//        this.addPlatform(NetflixPlatform::class.java)
        this.addPlatform(ScantradPlatform::class.java)
        this.addPlatform(WakanimPlatform::class.java)

        JLogger.info("Setup FCM...")

        if (FileImpl.fileExists("firebase_key.json")) {
            val options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(FileInputStream(FileImpl.getFile("firebase_key.json")))).setProjectId("866259759032").build()
            FirebaseApp.initializeApp(options)
        } else {
            JLogger.warning("FCM File not found, ignoring...")
        }

        JLogger.info("Setup all plugins...")
        PluginManager.loadAll()

        JLogger.info("Starting...")
        JThread.startExactly({
            val checkDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

            if (checkDay != day) {
                JLogger.info("Resetting checked episodes...")
                day = checkDay

                this.saveAnalytics()

                this.platforms.forEach {
                    it.platform.checkedEpisodes.clear()
                    it.platform.checkedData.clear()
                }
            }

            this.checkEpisodesAndScans()
        }, delay = 2 * 60 * 1000L, priority = Thread.MAX_PRIORITY)
    }

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

    private fun checkEpisodesAndScans(calendar: Calendar = Calendar.getInstance()) {
        Impl.tryCatch("Failed to fetch episodes") {
            JLogger.info("Check if has Internet...")
            if (Impl.hasInternet()) {
                val gson = Gson()
                val (episodesFile, episodesSaved) = getEpisodeFile(gson)
                val (scansFile, scansSaved) = getScanFile(gson)
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
                    val episodes = platformImpl.platform.checkEpisodes(calendar)
                    JLogger.info("[${platformImpl.platformHandler.name}] Fetching scans...")
                    val scans = platformImpl.platform.checkScans(calendar)
                    JLogger.config("[${platformImpl.platformHandler.name}] All fetched! Episodes length: ${episodes.size} - Scans length: ${scans.size}")

                    if (episodes.isNotEmpty()) {
                        JLogger.info("[${platformImpl.platformHandler.name}] Insert all episodes...")

                        Impl.tryCatch("[${platformImpl.platformHandler.name}] Cannot insert episodes!") {
                            episodes.sortedBy { it.releaseDate }.forEach { episode ->
                                if (!episodesSaved.contains(episode.episodeId)) {
                                    episodesSaved.add(episode.episodeId)
                                    Files.write(episodesFile.toPath(), gson.toJson(episodesSaved).toByteArray())

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
                }

                val endTime = System.currentTimeMillis()
                JLogger.info("All platforms has been checked in ${endTime - startTime}ms!")

                connection?.close()

                // TODO: New notification system
//                if (animes.isNotEmpty()) {
//                    val animeRelease = animes.values.joinToString(", ") { it }
//                    this.sendMessage("Nouvelle(s) sortie(s)", animeRelease)
//                    JLogger.info("New release: $animeRelease")
//                }
            } else {
                JLogger.warning("No internet")
            }
        }
    }

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

    private fun getScanFile(gson: Gson): Pair<File, MutableList<Int>> {
        val file = FileImpl.getFile("scans.json")

        if (!file.exists()) {
            file.createNewFile()
            Files.write(file.toPath(), gson.toJson(JsonArray()).toByteArray())
        }

        val episodesSaved =
            (gson.fromJson(FileReader(file), Array<Int>::class.java) ?: emptyArray()).toMutableList()
        return Pair(file, episodesSaved)
    }

    private fun sendMessage(title: String, description: String, image: String? = null) {
        FirebaseMessaging.getInstance().send(
            Message.builder().setAndroidConfig(
                AndroidConfig.builder().setNotification(
                    AndroidNotification.builder().setTitle(title).setBody(description).setImage(image).build()
                ).build()
            ).setTopic("animes").build()
        )
    }

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

    fun getCountryInformation(country: Country?): CountryImpl? {
        return if (country != null) this.countries.firstOrNull { it.country::class.java == country::class.java } else null
    }

    private fun getCountryInformation(country: KClass<out Country>?): CountryImpl? {
        return this.countries.firstOrNull { it.country::class.java == country?.java }
    }

    fun getPlatformInformation(platform: Platform?): PlatformImpl? {
        return if (platform != null) this.platforms.firstOrNull { it.platform::class.java == platform::class.java } else null
    }

    fun getAllowedCountries(platform: Platform?): Array<Country> {
        return this.getPlatformInformation(platform)?.platformHandler?.countries?.mapNotNull { platformCountry ->
            this.getCountryInformation(
                platformCountry
            )
        }?.map { it.country }?.toTypedArray() ?: arrayOf()
    }
}