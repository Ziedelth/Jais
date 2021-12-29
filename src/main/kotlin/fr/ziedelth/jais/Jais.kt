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
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.LangType
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

    init {
        JLogger.info("Init...")

        JLogger.info("Setup FCM...")
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(FileInputStream(FileImpl.getFile("firebase_key.json"))))
            .setProjectId("866259759032").build()
        FirebaseApp.initializeApp(options)

        JLogger.info("Setup all plugins...")
        PluginManager.loadAll()

        JLogger.info("Adding anime genres...")
        Impl.tryCatch {
            val connection = JMapper.getConnection()
            connection?.autoCommit = false

            Impl.tryCatch({
                Genre.values().forEach { JMapper.insertGenre(connection, it) }
                EpisodeType.values().forEach { JMapper.insertEpisodeType(connection, it) }
                LangType.values().forEach { JMapper.insertLangType(connection, it) }
                connection?.commit()
            }, {
                connection?.rollback()
            })

            connection?.close()
        }

        JLogger.info("Adding countries...")
        this.addCountry(FranceCountry::class.java)

        JLogger.info("Adding platforms...")
        this.addPlatform(AnimeDigitalNetworkPlatform::class.java)
        this.addPlatform(CrunchyrollPlatform::class.java)
        this.addPlatform(NetflixPlatform::class.java)
        this.addPlatform(ScantradPlatform::class.java)
        this.addPlatform(WakanimPlatform::class.java)

//        this.platforms.forEach { platform -> platform.platform.checkEpisodes().forEach { JLogger.config(it.toString()) } }
//        this.platforms.forEach { it.platform.checkScans() }

        JLogger.info("Starting...")

        JThread.start({
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
            val animes = mutableMapOf<String, String>()
            val connection = JMapper.getConnection()

            val episodesList = this.platforms.flatMap { it.platform.checkEpisodes(calendar).toList() }

            if (episodesList.isNotEmpty()) {
                episodesList.sortedBy { it.releaseDate }.forEach { episode ->
                    val platformData = JMapper.insertPlatform(
                        connection,
                        episode.platform.platformHandler
                    )
                    val countryData = JMapper.insertCountry(
                        connection,
                        episode.country.countryHandler
                    )

                    if (platformData != null && countryData != null) {
                        val animeData = JMapper.insertAnime(
                            connection,
                            countryData.id,
                            ISO8601.toUTCDate(ISO8601.fromCalendar(episode.releaseDate)),
                            episode.anime,
                            episode.animeImage,
                            episode.animeDescription
                        )

                        if (animeData != null) {
                            episode.animeGenres.forEach {
                                val genre = JMapper.insertGenre(connection, it)
                                if (genre != null) JMapper.insertAnimeGenre(connection, animeData.id, genre.id)
                            }

                            val etd = JMapper.insertEpisodeType(connection, episode.episodeType)
                            val ltd = JMapper.insertLangType(connection, episode.langType)

                            if (etd != null && ltd != null) {
                                val exists = JMapper.getEpisode(connection, episode.episodeId) != null
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
                                    if (!animes.contains(episode.anime.onlyLettersAndDigits()))
                                        animes[episode.anime.onlyLettersAndDigits()] = episode.anime

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
            }

            val scansList = this.platforms.flatMap { it.platform.checkScans(calendar).toList() }

            if (scansList.isNotEmpty()) {
                scansList.sortedBy { it.releaseDate }.forEach { scan ->
                    val platformData = JMapper.insertPlatform(
                        connection,
                        scan.platform.platformHandler
                    )
                    val countryData = JMapper.insertCountry(
                        connection,
                        scan.country.countryHandler
                    )

                    if (platformData != null && countryData != null) {
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
                                if (genre != null) JMapper.insertAnimeGenre(connection, animeData.id, genre.id)
                            }

                            val etd = JMapper.insertEpisodeType(connection, scan.episodeType)
                            val ltd = JMapper.insertLangType(connection, scan.langType)

                            if (etd != null && ltd != null) {
                                val exists =
                                    JMapper.getScan(
                                        connection,
                                        platformData.id,
                                        animeData.id,
                                        scan.number.toInt()
                                    ) != null
                                val episodeData = JMapper.insertScan(
                                    connection,
                                    platformData.id,
                                    animeData.id,
                                    etd.id,
                                    ltd.id,
                                    ISO8601.toUTCDate(ISO8601.fromCalendar(scan.releaseDate)),
                                    scan.number.toInt(),
                                    scan.url
                                )

                                if (!exists && episodeData != null) {
                                    if (!animes.contains(scan.anime.onlyLettersAndDigits()))
                                        animes[scan.anime.onlyLettersAndDigits()] = scan.anime

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
            }

            connection?.close()

            if (animes.isNotEmpty()) {
                val animeRelease = animes.values.joinToString(", ") { it }
                this.sendMessage("Nouvelle(s) sortie(s)", animeRelease)
                JLogger.info("New release: $animeRelease")
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

    private fun addCountry(country: Class<out Country>) {
        if (this.countries.none { it.country::class.java == country } && country.isAnnotationPresent(CountryHandler::class.java)) {
            this.countries.add(
                CountryImpl(
                    countryHandler = country.getAnnotation(CountryHandler::class.java),
                    country = country.getConstructor().newInstance()
                )
            )
        } else JLogger.warning("Failed to add ${country.simpleName}")
    }

    private fun addPlatform(platform: Class<out Platform>) {
        if (this.platforms.none { it.platform::class.java == platform } && platform.isAnnotationPresent(PlatformHandler::class.java)) {
            this.platforms.add(
                PlatformImpl(
                    platformHandler = platform.getAnnotation(PlatformHandler::class.java),
                    platform = platform.getConstructor().newInstance()
                )
            )
        } else JLogger.warning("Failed to add ${platform.simpleName}")
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