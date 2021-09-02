/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.clients

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Country
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News
import fr.ziedelth.jais.utils.clients.Client
import fr.ziedelth.jais.utils.tokens.TwitterToken
import org.pf4j.PluginWrapper
import twitter4j.StatusUpdate
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.util.*
import java.util.logging.Level
import kotlin.math.min

class TwitterClient(wrapper: PluginWrapper?) : Client(wrapper) {
    private var twitter: Twitter? = null

    init {
        val token = Const.getToken("twitter.json", TwitterToken::class.java) as TwitterToken?

        if (token != null) {
            try {
                val configurationBuilder = ConfigurationBuilder()
                configurationBuilder.setDebugEnabled(true).setOAuthConsumerKey(token.OAuthConsumerKey)
                    .setOAuthConsumerSecret(token.OAuthConsumerSecret)
                    .setOAuthAccessToken(token.OAuthAccessToken)
                    .setOAuthAccessTokenSecret(token.OAuthAccessTokenSecret)
                val twitterFactory = TwitterFactory(configurationBuilder.build())

                this.twitter = twitterFactory.instance

                JLogger.info(
                    "[${this.javaClass.simpleName}] Connected with ${
                        try {
                            this.twitter!!.getFollowersList(
                                this.twitter!!.screenName,
                                -1
                            ).size
                        } catch (twitterException: TwitterException) {
                            -1
                        }
                    } follower(s)!"
                )
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
                    val statusMessage = StatusUpdate(Const.getAnimesMessage(animes))
                    statusMessage.setMediaIds(
                        *images.take(min(4, images.size))
                            .map { this.twitter!!.uploadMedia("${UUID.randomUUID()}.jpg", it).mediaId }.toLongArray()
                    )

                    this.twitter!!.updateStatus(statusMessage)
                } catch (exception: Exception) {
                    JLogger.log(
                        Level.WARNING,
                        "Can not tweet episodes: ${exception.message ?: "Nothing..."}",
                        exception
                    )
                }
            } else {
                cl.forEach { episode ->
                    try {
                        val uploadedMedia =
                            this.twitter!!.uploadMedia("${UUID.randomUUID()}.jpg", episode.downloadedImage)

                        val statusMessage = StatusUpdate(Const.getEpisodeMessage(episode))
                        statusMessage.setMediaIds(uploadedMedia.mediaId)

                        this.twitter!!.updateStatus(statusMessage)
                    } catch (exception: Exception) {
                        JLogger.log(
                            Level.WARNING,
                            "Can not tweet episodes: ${exception.message ?: "Nothing..."}",
                            exception
                        )
                    }
                }
            }
        }
    }

    override fun sendNews(news: Array<News>) {
        news.filter { it.country == Country.FRANCE }.forEach {
            try {
                val statusMessage = StatusUpdate(
                    "${
                        Const.substring(
                            it.title,
                            100
                        )
                    }${if (it.title.length < 100) "" else "..."}\n#News #${
                        it.platform.replace(
                            " ",
                            ""
                        )
                    }\n\nLire l'actualité :\n${it.link}"
                )

                this.twitter!!.updateStatus(statusMessage)
            } catch (twitterException: TwitterException) {
                JLogger.log(
                    Level.WARNING,
                    "Can not tweet news: ${twitterException.message ?: "Nothing..."}",
                    twitterException
                )
            }
        }
    }
}
