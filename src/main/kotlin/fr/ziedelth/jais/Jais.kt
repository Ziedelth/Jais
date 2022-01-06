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
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.platforms.*
import fr.ziedelth.jais.utils.*
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import fr.ziedelth.jais.utils.animes.sql.JMapper
import fr.ziedelth.jais.utils.plugins.PluginManager
import fr.ziedelth.jais.utils.plugins.PluginUtils.onlyLettersAndDigits
import java.io.FileInputStream
import java.util.*
import kotlin.reflect.KClass

class Jais {
    private val countries = mutableListOf<CountryImpl>()
    private val platforms = mutableListOf<PlatformImpl>()
    private var day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    fun init() {
        JLogger.info("Init...")

        JLogger.info("Adding countries...")
        this.addCountry(FranceCountry::class.java)

        JLogger.info("Adding platforms...")
        this.addPlatform(AnimeDigitalNetworkPlatform::class.java)
        this.addPlatform(CrunchyrollPlatform::class.java)
        this.addPlatform(NetflixPlatform::class.java)
        this.addPlatform(ScantradPlatform::class.java)
        this.addPlatform(WakanimPlatform::class.java)

        JLogger.info("Setup FCM...")
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(FileInputStream(FileImpl.getFile("firebase_key.json"))))
            .setProjectId("866259759032").build()
        FirebaseApp.initializeApp(options)

        JLogger.info("Setup all plugins...")
        PluginManager.loadAll()

        JLogger.info("Starting...")
        JThread.startExactly({
            val checkDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

            if (checkDay != day) {
                JLogger.info("Resetting checked episodes...")
                day = checkDay
                this.platforms.forEach { it.platform.checkedEpisodes.clear(); it.platform.checkedData.clear() }
            }

            this.checkEpisodesAndScans()
        }, delay = 2 * 60 * 1000L, priority = Thread.MAX_PRIORITY)
    }

    private fun checkEpisodesAndScans(calendar: Calendar = Calendar.getInstance()) {
        Impl.tryCatch("Failed to fetch episodes") {
            JLogger.info("Check if has Internet...")
            if (Impl.hasInternet()) {
                val animes = mutableMapOf<String, String>()
                val connection = JMapper.getConnection()
                val startTime = System.currentTimeMillis()

                JThread.startMultiThreads(this.platforms.map { platformImpl ->
                    {
                        JLogger.info("[${platformImpl.platformHandler.name}] Fetching episodes...")
                        val episodes = platformImpl.platform.checkEpisodes(calendar)
                        JLogger.info("[${platformImpl.platformHandler.name}] Fetching scans...")
                        val scans = platformImpl.platform.checkScans(calendar)
                        JLogger.config("[${platformImpl.platformHandler.name}] All fetched! Episodes length: ${episodes.size} - Scans length: ${scans.size}")

                        if (episodes.isNotEmpty()) {
                            JLogger.info("[${platformImpl.platformHandler.name}] Insert all episodes...")

                            Impl.tryCatch("[${platformImpl.platformHandler.name}] Cannot insert episodes!") {
                                JLogger.config("Insert platform data...")
                                val platformData = JMapper.insertPlatform(connection, platformImpl.platformHandler)

                                episodes.sortedBy { it.releaseDate }.forEach { episode ->
                                    JLogger.config("Insert country data...")
                                    val countryData = JMapper.insertCountry(connection, episode.country.countryHandler)

                                    if (platformData != null && countryData != null) {
                                        JLogger.config("Insert anime data...")
                                        val animeData = JMapper.insertAnime(
                                            connection,
                                            countryData.id,
                                            ISO8601.toUTCDate(ISO8601.fromCalendar(episode.releaseDate)),
                                            episode.anime,
                                            episode.animeImage,
                                            episode.animeDescription
                                        )

                                        if (animeData != null) {
                                            JLogger.config("Insert anime genres data...")
                                            episode.animeGenres.forEach {
                                                val genre = JMapper.insertGenre(connection, it)
                                                if (genre != null) JMapper.insertAnimeGenre(
                                                    connection,
                                                    animeData.id,
                                                    genre.id
                                                )
                                            }

                                            JLogger.config("Insert episode type data...")
                                            val etd = JMapper.insertEpisodeType(connection, episode.episodeType)
                                            JLogger.config("Insert lang type data...")
                                            val ltd = JMapper.insertLangType(connection, episode.langType)

                                            if (etd != null && ltd != null) {
                                                val exists = JMapper.getEpisode(connection, episode.episodeId) != null
                                                JLogger.config("Insert episode data...")
                                                val episodeData = JMapper.insertEpisode(
                                                    connection,
                                                    platformData.id,
                                                    animeData.id,
                                                    etd.id,
                                                    ltd.id,
                                                    ISO8601.toUTCDate(ISO8601.fromCalendar(episode.releaseDate)),
                                                    episode.season.toInt(),
                                                    episode.number.toInt(),
                                                    episode.episodeId,
                                                    episode.title,
                                                    episode.url,
                                                    episode.image,
                                                    episode.duration
                                                )

                                                if (!exists && episodeData != null) {
                                                    JLogger.config("Episode don't exists before, send to plugins...")
                                                    if (!animes.contains(episode.anime.onlyLettersAndDigits())) animes[episode.anime.onlyLettersAndDigits()] =
                                                        episode.anime

                                                    Impl.tryCatch {
                                                        PluginManager.plugins?.forEach {
                                                            it.newEpisode(episode)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                JLogger.config("Ok!")
                            }

                            JLogger.info("[${platformImpl.platformHandler.name}] All episodes has been inserted!")
                        }

                        if (scans.isNotEmpty()) {
                            JLogger.info("[${platformImpl.platformHandler.name}] Insert all scans...")

                            Impl.tryCatch("[${platformImpl.platformHandler.name}] Cannot insert scans!") {
                                JLogger.config("Insert platform data...")
                                val platformData = JMapper.insertPlatform(connection, platformImpl.platformHandler)

                                scans.sortedBy { it.releaseDate }.forEach { scan ->
                                    JLogger.config("Insert country data...")
                                    val countryData = JMapper.insertCountry(connection, scan.country.countryHandler)

                                    if (platformData != null && countryData != null) {
                                        JLogger.config("Insert anime genres data...")
                                        val animeData = JMapper.insertAnime(
                                            connection,
                                            countryData.id,
                                            ISO8601.toUTCDate(ISO8601.fromCalendar(scan.releaseDate)),
                                            scan.anime,
                                            scan.animeImage,
                                            scan.animeDescription
                                        )

                                        if (animeData != null) {
                                            scan.animeGenres.forEach {
                                                val genre = JMapper.insertGenre(connection, it)
                                                if (genre != null) JMapper.insertAnimeGenre(
                                                    connection,
                                                    animeData.id,
                                                    genre.id
                                                )
                                            }

                                            JLogger.config("Insert episode type data...")
                                            val etd = JMapper.insertEpisodeType(connection, scan.episodeType)
                                            JLogger.config("Insert lang type data...")
                                            val ltd = JMapper.insertLangType(connection, scan.langType)

                                            if (etd != null && ltd != null) {
                                                val exists = JMapper.getScan(
                                                    connection,
                                                    platformData.id,
                                                    animeData.id,
                                                    scan.number.toInt()
                                                ) != null
                                                JLogger.config("Insert scan data...")
                                                val scanData = JMapper.insertScan(
                                                    connection,
                                                    platformData.id,
                                                    animeData.id,
                                                    etd.id,
                                                    ltd.id,
                                                    ISO8601.toUTCDate(ISO8601.fromCalendar(scan.releaseDate)),
                                                    scan.number.toInt(),
                                                    scan.url
                                                )

                                                if (!exists && scanData != null) {
                                                    JLogger.config("Scan don't exists before, send to plugins...")
                                                    if (!animes.contains(scan.anime.onlyLettersAndDigits())) animes[scan.anime.onlyLettersAndDigits()] =
                                                        scan.anime

                                                    Impl.tryCatch {
                                                        PluginManager.plugins?.forEach {
                                                            it.newScan(scan)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                JLogger.config("Ok!")
                            }

                            JLogger.info("[${platformImpl.platformHandler.name}] All scans has been inserted!")
                        }
                    }
                })

                val endTime = System.currentTimeMillis()
                JLogger.info("All platforms has been checked in ${endTime - startTime}ms!")

                connection?.close()

                if (animes.isNotEmpty()) {
                    val animeRelease = animes.values.joinToString(", ") { it }
                    this.sendMessage("Nouvelle(s) sortie(s)", animeRelease)
                    JLogger.info("New release: $animeRelease")
                }
            }
        }
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