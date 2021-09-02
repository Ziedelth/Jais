/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.clients

import com.github.instagram4j.instagram4j.IGClient
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Country
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.clients.Client
import fr.ziedelth.jais.utils.tokens.InstagramToken
import org.pf4j.PluginWrapper
import java.util.logging.Level

class InstagramClient(wrapper: PluginWrapper?) : Client(wrapper) {
    private var igClient: IGClient? = null

    init {
        val token = Const.getToken("instagram.json", InstagramToken::class.java) as InstagramToken?

        if (token != null) {
            try {
                this.igClient = IGClient.builder().username(token.username).password(token.password).login()
                JLogger.info("[${this.javaClass.simpleName}] Updated!")
            } catch (exception: Exception) {
                JLogger.log(Level.WARNING, "Can not load ${this.javaClass.simpleName} client", exception)
            }
        }
    }

    override fun sendEpisodes(episodes: Array<Episode>) {
        episodes.map { it.platform }.distinct().forEach { platform ->
            val cl = episodes.filter { it.country == Country.FRANCE && it.platform == platform }.toTypedArray()

            // SPAM
            if (cl.size >= 10) {
                val animes = Const.getAnimes(cl)
                val images = Const.getImages(animes, cl)

                try {
                    images.forEach {
                        val bytes = it.readAllBytes()

                        this.igClient!!.actions().timeline()!!.uploadPhoto(bytes, Const.getAnimesMessage(animes))
                        this.igClient!!.actions().story()!!.uploadPhoto(bytes, arrayListOf())
                    }
                } catch (exception: Exception) {
                    JLogger.log(
                        Level.WARNING,
                        "Can not publish episodes: ${exception.message ?: "Nothing..."}",
                        exception
                    )
                }
            } else {
                cl.forEach { episode ->
                    try {
                        this.igClient!!.actions().timeline()!!
                            .uploadPhoto(episode.downloadedImage?.readAllBytes(), Const.getEpisodeMessage(episode))
                    } catch (exception: Exception) {
                        JLogger.log(
                            Level.WARNING,
                            "Can not publish episodes: ${exception.message ?: "Nothing..."}",
                            exception
                        )
                    }
                }
            }
        }
    }
}