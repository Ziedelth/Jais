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
import twitter4j.StatusUpdate
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.util.*
import java.util.logging.Level
import kotlin.math.min

class TwitterClient : Client {
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
                this.update()
            } catch (exception: Exception) {
                JLogger.log(Level.WARNING, "Can not load ${this.javaClass.simpleName} client", exception)
            }
        }
    }

    override fun update() {
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
    }

    override fun sendEpisodes(episodes: Array<Episode>) {
        episodes.map { it.platform }.distinct().forEach { platform ->
            val cl = episodes.filter { it.country == Country.FRANCE && it.platform == platform }

            // SPAM
            if (cl.size >= 10) {
                val animes: Array<String> = cl.map { it.anime }.distinct().toTypedArray()
                val stringBuilder = animes.map { "• $it\n" }.distinct().toString()

                try {
                    val statusMessage = StatusUpdate(stringBuilder)
                    statusMessage.setMediaIds()
                    statusMessage.setMediaIds(*animes.slice(0..min(animes.size, 5))
                        .mapNotNull { anime -> cl.firstOrNull { episode -> episode.anime.equals(anime, true) } }
                        .map { episode ->
                            this.twitter!!.uploadMedia(
                                "${
                                    UUID.randomUUID().toString().replace("-", "")
                                }.jpg", episode.downloadedImage
                            ).mediaId
                        }.toLongArray()
                    )

                    this.twitter!!.updateStatus(statusMessage)
                } catch (exception: Exception) {
                    JLogger.warning("Can not tweet episodes: ${exception.message ?: "Nothing..."}")
                }
            } else {
                cl.forEach { episode ->
                    try {
                        val statusMessage = StatusUpdate(Const.getEpisodeMessage(episode))
                        statusMessage.setMediaIds(
                            this.twitter!!.uploadMedia(
                                "${
                                    UUID.randomUUID().toString().replace("-", "")
                                }.jpg", episode.downloadedImage
                            ).mediaId
                        )

                        this.twitter!!.updateStatus(statusMessage)
                    } catch (exception: Exception) {
                        JLogger.warning("Can not tweet episodes: ${exception.message ?: "Nothing..."}")
                    }
                }
            }
        }
    }

    override fun sendNews(news: Array<News>) {
        news.filter { it.country == Country.FRANCE }.forEach {
            try {
                val statusMessage = StatusUpdate(
                    "${Const.substring(it.title, 100)}${if (it.title.length < 100) "" else "..."}\n#News #${
                        it.platform.replace(
                            " ",
                            ""
                        )
                    }\n\nLire l'actualité :\n${it.link}"
                )

                this.twitter!!.updateStatus(statusMessage)
            } catch (twitterException: TwitterException) {
                JLogger.warning("Can not tweet news: ${twitterException.message ?: "Nothing..."}")
            }
        }
    }
}
