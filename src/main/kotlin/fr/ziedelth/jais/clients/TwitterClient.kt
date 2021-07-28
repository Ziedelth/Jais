package fr.ziedelth.jais.clients

import com.google.gson.JsonObject
import fr.ziedelth.jais.utils.Client
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Country
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.News
import fr.ziedelth.jais.utils.tokens.TwitterToken
import twitter4j.StatusUpdate
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.text.SimpleDateFormat
import javax.imageio.ImageIO

class TwitterClient : Client {
    private val file = File("twitter.json")
    private val obj: JsonObject = if (!this.file.exists()) JsonObject() else Const.GSON.fromJson(
        Files.readString(
            this.file.toPath(),
            Const.DEFAULT_CHARSET
        ), JsonObject::class.java
    )
    private val tokenFile = File(Const.TOKENS_FOLDER, "twitter.json")
    private val init: Boolean
    private var twitter: Twitter? = null

    init {
        if (!this.tokenFile.exists()) {
            this.init = false
            JLogger.warning("Twitter token not exists!")
            Files.writeString(this.tokenFile.toPath(), Const.GSON.toJson(TwitterToken()), Const.DEFAULT_CHARSET)
        } else {
            val twitterToken = Const.GSON.fromJson(
                Files.readString(
                    this.tokenFile.toPath(),
                    Const.DEFAULT_CHARSET
                ), TwitterToken::class.java
            )

            if (twitterToken.OAuthConsumerKey.isEmpty() || twitterToken.OAuthConsumerSecret.isEmpty() || twitterToken.OAuthAccessToken.isEmpty() || twitterToken.OAuthAccessTokenSecret.isEmpty()) {
                this.init = false
                JLogger.warning("Twitter token is empty!")
            } else {
                this.init = true
                val configurationBuilder = ConfigurationBuilder()
                configurationBuilder.setDebugEnabled(true).setOAuthConsumerKey(twitterToken.OAuthConsumerKey)
                    .setOAuthConsumerSecret(twitterToken.OAuthConsumerSecret)
                    .setOAuthAccessToken(twitterToken.OAuthAccessToken)
                    .setOAuthAccessTokenSecret(twitterToken.OAuthAccessTokenSecret)
                val twitterFactory = TwitterFactory(configurationBuilder.build())

                this.twitter = twitterFactory.instance
                this.update()
            }
        }
    }

    override fun update() {
        JLogger.info(
            "[${this.javaClass.simpleName}] Connected with ${
                this.twitter!!.getFollowersList(
                    this.twitter!!.screenName,
                    -1
                ).size
            } follower(s)!"
        )
    }

    override fun sendEpisode(episodes: Array<Episode>, new: Boolean) {
        if (!this.init) return
        val episodesObj: JsonObject = this.obj["episodes"]?.asJsonObject ?: JsonObject()
        val countryEpisodes = episodes.filter { it.country == Country.FRANCE }

        if (new) {
            val size = countryEpisodes.size

            if (size <= 12) {
                countryEpisodes.forEach {
                    val uploadMedia = this.twitter!!.uploadMedia("${it.globalId}.jpg", downloadImageEpisode(it))

                    val statusMessage = StatusUpdate(
                        "🔜 ${it.anime}\n${if (it.title != null) "${it.title}\n" else ""}${it.country.episode} ${it.number} ${if (it.type == EpisodeType.SUBTITLED) it.country.subtitled else it.country.dubbed}\n\uD83C\uDFAC ${
                            SimpleDateFormat(
                                "mm:ss"
                            ).format(it.duration * 1000)
                        }\n#Anime #${
                            it.platform.replace(
                                " ",
                                ""
                            )
                        }\n\n${it.link}"
                    )
                    statusMessage.setMediaIds(uploadMedia.mediaId)

                    val tweet = this.twitter!!.updateStatus(statusMessage)
                    episodesObj.addProperty(it.globalId, tweet.id)
                }

                if (size > 0) {
                    this.obj.add("episodes", episodesObj)
                    Files.writeString(this.file.toPath(), Const.GSON.toJson(this.obj), Const.DEFAULT_CHARSET)
                }
            } else {
                this.twitter!!.updateStatus("Il y a trop d'animes qui sortent en même temps que je m'en perds dans mes circuits ! (${size}, c'est beaucoup trop pour moi)")
            }
        } else {
            countryEpisodes.forEach {
                if (episodesObj.has(it.globalId)) {
                    val oldTweet = episodesObj[it.globalId]!!.asLong
                    val uploadMedia = this.twitter!!.uploadMedia("${it.globalId}.jpg", downloadImageEpisode(it))

                    val statusMessage =
                        StatusUpdate(
                            "Modification :\n${if (it.title != null) "${it.title}\n" else ""}🎬 ${
                                SimpleDateFormat("mm:ss").format(
                                    it.duration * 1000
                                )
                            }\n\n${it.link}"
                        )
                    statusMessage.setMediaIds(uploadMedia.mediaId)
                    statusMessage.inReplyToStatusId = oldTweet

                    this.twitter!!.updateStatus(statusMessage)
                }
            }
        }
    }

    private fun downloadImageEpisode(episode: Episode): ByteArrayInputStream {
        val bufferedImage = ImageIO.read(URL(episode.image))
        val baos = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "jpg", baos)
        return ByteArrayInputStream(baos.toByteArray())
    }

    override fun sendNews(news: Array<News>) {
        if (!this.init) return

        news.filter { it.country == Country.FRANCE }.forEach {
            val statusMessage = StatusUpdate(
                "${it.title}\n#${
                    it.platform.replace(
                        " ",
                        ""
                    )
                }\n\n${it.link}"
            )

            this.twitter!!.updateStatus(statusMessage)
        }
    }
}