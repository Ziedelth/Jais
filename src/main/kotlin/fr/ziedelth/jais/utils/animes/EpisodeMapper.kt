/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.episodes.AnimeGenre
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.LangType
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.io.File
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

data class EpisodeMapper(
    var platforms: MutableList<PlatformImpl> = mutableListOf(),
    var countries: MutableList<CountryImpl> = mutableListOf(),
    var animes: MutableList<AnimeImpl> = mutableListOf(),
) {
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
    ) {
        constructor(uuid: String, countryHandler: CountryHandler) : this(
            uuid = uuid,
            name = countryHandler.name,
            flag = countryHandler.flag,
            season = countryHandler.season,
        )
    }

    data class AnimeImpl(
        val countryId: String,
        val uuid: String,
        val releaseDate: String,
        val name: String,
        var image: String?,
        var genres: MutableList<AnimeGenre>,
        var episodes: MutableList<EpisodeImpl>
    )

    data class EpisodeImpl(
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

    fun update() {
        this.animes = this.animes.sortedBy { it.name }.toMutableList()
        this.animes.forEach { aImpl -> aImpl.episodes =
            aImpl.episodes.sortedWith(Comparator.comparing { episodeIImpl: EpisodeImpl -> episodeIImpl.season }
                .thenComparing(EpisodeImpl::number)).toMutableList()
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

    private fun getLastNumberEpisodeType(countryId: String, anime: String, episodeIImpl: EpisodeImpl): Long =
        this.getAnime(
            countryId,
            anime
        )?.episodes?.filter { it.season == episodeIImpl.season && it.episodeType == episodeIImpl.episodeType && it.langType == episodeIImpl.langType }
            ?.maxByOrNull { it.number }?.number ?: 0

    fun insertOrUpdateEpisode(platformId: String, countryId: String, episode: Episode): Boolean {
        val episodeIImpl = EpisodeImpl(platformId, UUID.randomUUID().toString(), episode)
        if (episodeIImpl.episodeType != EpisodeType.EPISODE) episodeIImpl.number =
            this.getLastNumberEpisodeType(countryId, episode.anime, episodeIImpl) + 1

        if (!this.animes.any { aImpl -> aImpl.episodes.any { it.eId == episodeIImpl.eId } }) {
            // Épisode non existant dans aucun anime, création...

            if (this.hasAnime(countryId, episode.anime)) {
                val aImpl = this.getAnime(countryId, episode.anime)!!

                if (aImpl.image.isNullOrEmpty() && episode.animeImage?.isNotEmpty() == true) {
                    aImpl.image = episode.animeImage
                    aImpl.image = fetchAnimeImage(aImpl)
                }

                if (aImpl.genres.isEmpty() && episode.animeGenres.isNotEmpty()) {
                    aImpl.genres = episode.animeGenres.toMutableList()
                }

                episodeIImpl.image = fetchEpisodeImage(episodeIImpl)

                aImpl.episodes.add(episodeIImpl)
            } else {
                val aImpl = AnimeImpl(
                    countryId,
                    UUID.randomUUID().toString(),
                    episode.releaseDate,
                    episode.anime,
                    episode.animeImage,
                    episode.animeGenres.toMutableList(),
                    mutableListOf(episodeIImpl)
                )

                aImpl.image = fetchAnimeImage(aImpl)
                episodeIImpl.image = fetchEpisodeImage(episodeIImpl)

                this.animes.add(aImpl)
            }

            return true
        } else {
            // Épisode existant dans un anime, récupération...
            val aep =
                this.animes.find { aImpl -> aImpl.episodes.any { it.eId == episodeIImpl.eId } }?.episodes?.find { it.eId == episodeIImpl.eId }

            if (aep?.title != episodeIImpl.title) aep?.title = episodeIImpl.title
            if (aep?.url != episodeIImpl.url) aep?.url = episodeIImpl.url

            if (aep?.image != episodeIImpl.image) {
                aep?.image = episodeIImpl.image
                episodeIImpl.image = fetchEpisodeImage(episodeIImpl)
            }

            if (aep?.duration != episodeIImpl.duration) aep?.duration = episodeIImpl.duration
        }

        return false
    }

    private fun fetchAnimeImage(animeImpl: AnimeImpl): String? {
        var path: String? = null
        if (animeImpl.image.isNullOrEmpty()) return path

        Impl.tryCatch("Failed to fetch anime image ${animeImpl.name}") {
            val folder = FileImpl.directories("images", "animes")
            val file = File(folder, "${animeImpl.uuid}.jpg")

            val image = FileImpl.resizeImage(ImageIO.read(URL(animeImpl.image)), 350, 500)
            ImageIO.write(image, "jpg", file)

            path = file.path
        }

        return path
    }

    private fun fetchEpisodeImage(episodeIImpl: EpisodeImpl): String? {
        var path: String? = null
        if (episodeIImpl.image.isNullOrEmpty()) return path

        Impl.tryCatch("Failed to fetch episode image ${episodeIImpl.eId}") {
            val folder = FileImpl.directories("images", "episodes")
            val file = File(folder, "${episodeIImpl.uuid}.jpg")

            val image = FileImpl.resizeImage(ImageIO.read(URL(episodeIImpl.image)), 640, 360)
            ImageIO.write(image, "jpg", file)

            path = file.path
        }

        return path
    }
}