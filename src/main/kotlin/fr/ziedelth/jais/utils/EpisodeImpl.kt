/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.LangType
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

const val DESIRED_WEIGHT = 50 * 1024 // In ib (1024 format)
private val EPISODE_COMPARATOR =
    Comparator.comparing { episodeIImpl: EpisodeIImpl -> episodeIImpl.season }.thenComparing(EpisodeIImpl::number)

data class EpisodeImpl(
    var platforms: MutableList<PlatformImpl> = mutableListOf(),
    var countries: MutableList<CountryImpl> = mutableListOf(),
    var animes: MutableList<AnimeImpl> = mutableListOf(),
) {
    fun update() {
        this.animes = this.animes.sortedBy { ISO8601.toCalendar1(it.releaseDate) }.toMutableList()

        this.animes.forEach { aImpl ->
            aImpl.episodes = aImpl.episodes.sortedWith(EPISODE_COMPARATOR).toMutableList()

            fetchAnimeImage(aImpl)
            aImpl.episodes.forEach { eImpl -> fetchEpisodeImage(eImpl) }
        }
    }

    private fun hasPlatform(name: String): Boolean =
        this.platforms.any { it.name.lowercase().contains(name.lowercase()) }

    private fun getPlatform(name: String): PlatformImpl? =
        this.platforms.find { it.name.lowercase().contains(name.lowercase()) }

    fun insertOrUpdatePlatform(platformHandler: PlatformHandler): PlatformImpl {
        return if (this.hasPlatform(platformHandler.name)) {
            val pImpl = this.getPlatform(platformHandler.name)!!

            if (pImpl.url != platformHandler.url) pImpl.url = platformHandler.url
            if (pImpl.image != platformHandler.image) pImpl.image = platformHandler.image
            if (pImpl.color != platformHandler.color) pImpl.color = platformHandler.color

            pImpl
        } else {
            val platformImpl = PlatformImpl(UUID.randomUUID().toString(), platformHandler)
            this.platforms.add(platformImpl)
            platformImpl
        }
    }

    private fun hasCountry(name: String): Boolean =
        this.countries.any { it.name.lowercase().contains(name.lowercase()) }

    private fun getCountry(name: String): CountryImpl? =
        this.countries.find { it.name.lowercase().contains(name.lowercase()) }

    fun insertOrUpdateCountry(countryHandler: CountryHandler): CountryImpl {
        return if (this.hasCountry(countryHandler.name)) {
            val cImpl = this.getCountry(countryHandler.name)!!

            if (cImpl.flag != countryHandler.flag) cImpl.flag = countryHandler.flag
            if (cImpl.season != countryHandler.season) cImpl.season = countryHandler.season
            if (cImpl.episode != countryHandler.episode) cImpl.episode = countryHandler.episode
            if (cImpl.film != countryHandler.film) cImpl.film = countryHandler.film
            if (cImpl.special != countryHandler.special) cImpl.special = countryHandler.special
            if (cImpl.subtitles != countryHandler.subtitles) cImpl.subtitles = countryHandler.subtitles
            if (cImpl.dubbed != countryHandler.dubbed) cImpl.dubbed = countryHandler.dubbed

            cImpl
        } else {
            val countryImpl = CountryImpl(UUID.randomUUID().toString(), countryHandler)
            this.countries.add(countryImpl)
            countryImpl
        }
    }

    private fun hasAnime(countryId: String, anime: String): Boolean =
        this.animes.any { it.countryId == countryId && it.name.lowercase().contains(anime.lowercase()) }

    private fun getAnime(countryId: String, anime: String): AnimeImpl? =
        this.animes.find { it.countryId == countryId && it.name.lowercase().contains(anime.lowercase()) }

    private fun getLastNumberEpisodeType(countryId: String, anime: String, episodeIImpl: EpisodeIImpl): Long =
        this.getAnime(
            countryId,
            anime
        )?.episodes?.filter { it.season == episodeIImpl.season && it.episodeType == episodeIImpl.episodeType && it.langType == episodeIImpl.langType }
            ?.maxByOrNull { it.number }?.number ?: 0

    fun insertOrUpdateEpisode(platformId: String, countryId: String, episode: Episode) {
        val episodeIImpl = EpisodeIImpl(platformId, UUID.randomUUID().toString(), episode)
        if (episodeIImpl.episodeType != EpisodeType.EPISODE) episodeIImpl.number =
            this.getLastNumberEpisodeType(countryId, episode.anime, episodeIImpl) + 1

        if (!this.animes.any { aImpl -> aImpl.episodes.any { it.eId == episodeIImpl.eId } }) {
            // Épisode non existant dans aucun anime, création...

            if (this.hasAnime(countryId, episode.anime)) {
                val aImpl = this.getAnime(countryId, episode.anime)!!

                if (aImpl.image.isNullOrEmpty() && episode.animeImage?.isNotEmpty() == true) {
                    aImpl.image = episode.animeImage
                    fetchAnimeImage(aImpl)
                }

                fetchEpisodeImage(episodeIImpl)

                aImpl.episodes.add(episodeIImpl)
            } else {
                val aImpl = AnimeImpl(
                    countryId,
                    UUID.randomUUID().toString(),
                    episode.releaseDate,
                    episode.anime,
                    episode.animeImage,
                    mutableListOf(episodeIImpl)
                )

                fetchAnimeImage(aImpl)
                fetchEpisodeImage(episodeIImpl)

                this.animes.add(aImpl)
            }
        } else {
            // Épisode existant dans un anime, récupération...
            val aImpl = this.animes.find { aImpl -> aImpl.episodes.any { it.eId == episodeIImpl.eId } }
            val aep = aImpl?.episodes?.find { it.eId == episodeIImpl.eId }

            if (aep?.title != episodeIImpl.title) aep?.title = episodeIImpl.title
            if (aep?.url != episodeIImpl.url) aep?.url = episodeIImpl.url

            if (aep?.image != episodeIImpl.image) {
                aep?.image = episodeIImpl.image
                fetchEpisodeImage(episodeIImpl)
            }

            if (aep?.duration != episodeIImpl.duration) aep?.duration = episodeIImpl.duration
        }
    }

    private fun fetchAnimeImage(animeImpl: AnimeImpl) {
        val file = File(FileImpl.directories("images", "animes"), "${animeImpl.uuid}.jpg")

        if (FileImpl.notExists(file) && !animeImpl.image.isNullOrEmpty()) {
            Impl.tryCatch("Failed to fetch anime image ${animeImpl.name}") {
                JLogger.info("Fetching animeImpl image ${animeImpl.name}...")
                val quality = this.compress(animeImpl.image, 350, 500, file)
                JLogger.config(
                    "Anime image ${animeImpl.name} quality -> ${
                        String.format(
                            "%.2f",
                            quality * 100
                        )
                    }% ${FileImpl.toFormat(file.length())}"
                )
            }
        }
    }

    private fun fetchEpisodeImage(episodeIImpl: EpisodeIImpl) {
        val file = File(FileImpl.directories("images", "episodes"), "${episodeIImpl.uuid}.jpg")

        if (FileImpl.notExists(file) && !episodeIImpl.image.isNullOrEmpty()) {
            Impl.tryCatch("Failed to fetch episode image ${episodeIImpl.eId}") {
                JLogger.info("Fetching episode image ${episodeIImpl.eId}...")
                val quality = this.compress(episodeIImpl.image, 640, 360, file)
                JLogger.config(
                    "Episode image ${episodeIImpl.eId} quality -> ${
                        String.format(
                            "%.2f",
                            quality * 100
                        )
                    }% ${FileImpl.toFormat(file.length())}"
                )
            }
        }
    }

    private fun compress(image: String?, width: Int, height: Int, file: File): Float {
        val resizedImage = FileImpl.resizeImage(ImageIO.read(URL(image)), width, height)
        var quality = -1f

        do {
            if (quality == -1f) quality = .75f
            else quality -= .01f

            FileImpl.compressImage(resizedImage, FileOutputStream(file), quality)
        } while (file.length() > DESIRED_WEIGHT)

        return quality
    }
}

data class PlatformImpl(
    val uuid: String,
    val name: String,
    var url: String,
    var image: String,
    var color: Int,
) {
    constructor(uuid: String, platformHandler: PlatformHandler) : this(
        uuid = uuid,
        name = platformHandler.name,
        url = platformHandler.url,
        image = platformHandler.image,
        color = platformHandler.color,
    )
}

data class CountryImpl(
    val uuid: String,
    val name: String,
    var flag: String,
    var season: String,
    var episode: String,
    var film: String,
    var special: String,
    var subtitles: String,
    var dubbed: String
) {
    constructor(uuid: String, countryHandler: CountryHandler) : this(
        uuid = uuid,
        name = countryHandler.name,
        flag = countryHandler.flag,
        season = countryHandler.season,
        episode = countryHandler.episode,
        film = countryHandler.film,
        special = countryHandler.special,
        subtitles = countryHandler.subtitles,
        dubbed = countryHandler.dubbed,
    )
}

data class AnimeImpl(
    val countryId: String,
    val uuid: String,
    val releaseDate: String,
    val name: String,
    var image: String?,
    var episodes: MutableList<EpisodeIImpl>
)

data class EpisodeIImpl(
    val platformId: String,
    val uuid: String,
    val releaseDate: String,
    val season: Long,
    var number: Long,
    val episodeType: EpisodeType,
    val langType: LangType,
    val eId: String,

    var title: String?,
    var url: String?,
    var image: String?,
    var duration: Long
) {
    constructor(platformId: String, uuid: String, episode: Episode) : this(
        platformId = platformId,
        uuid = uuid,
        releaseDate = episode.releaseDate,
        season = episode.season,
        number = episode.number,
        episodeType = episode.episodeType,
        langType = episode.langType,
        eId = episode.eId,
        title = episode.title,
        url = episode.url,
        image = episode.image,
        duration = episode.duration
    )
}