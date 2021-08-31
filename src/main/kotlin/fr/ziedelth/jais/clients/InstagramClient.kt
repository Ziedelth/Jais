/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.clients

import com.github.instagram4j.instagram4j.IGClient
import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Country
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.clients.Client
import fr.ziedelth.jais.utils.tokens.InstagramToken
import java.util.logging.Level
import kotlin.math.min

class InstagramClient : Client {
    private var igClient: IGClient? = null

    init {
        val token = Const.getToken("instagram.json", InstagramToken::class.java) as InstagramToken?

        if (token != null) {
            try {
                this.igClient = IGClient.builder().username(token.username).password(token.password).login()
                this.update()
            } catch (exception: Exception) {
                JLogger.log(Level.WARNING, "Can not load ${this.javaClass.simpleName} client", exception)
            }
        }
    }

    override fun update() {
        JLogger.info("[${this.javaClass.simpleName}] Updated!")
    }

    override fun sendEpisodes(episodes: Array<Episode>) {
        episodes.map { it.platform }.distinct().forEach { platform ->
            val cl = episodes.filter { it.country == Country.FRANCE && it.platform == platform }

            // SPAM
            if (cl.size >= 10) {
                val animes: Array<String> = cl.map { it.anime }.distinct().toTypedArray()
                val stringBuilder = animes.map { "• $it\n" }.distinct().toString()

                try {
                    this.igClient!!.actions().timeline()!!.uploadAlbum(
                        animes.slice(0..min(animes.size, 5))
                            .mapNotNull { anime -> cl.firstOrNull { episode -> episode.anime.equals(anime, true) } }
                            .map { episode -> TimelineAction.SidecarPhoto(episode.downloadedImage.readAllBytes()) },
                        stringBuilder
                    )
                } catch (exception: Exception) {
                    JLogger.warning("Can not publish episodes: ${exception.message ?: "Nothing..."}")
                }
            } else {
                cl.forEach { episode ->
                    try {
                        this.igClient!!.actions().timeline()!!
                            .uploadPhoto(episode.downloadedImage.readAllBytes(), Const.getEpisodeMessage(episode))
                    } catch (exception: Exception) {
                        JLogger.warning("Can not publish episodes: ${exception.message ?: "Nothing..."}")
                    }
                }
            }
        }
    }
}