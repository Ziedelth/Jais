package fr.ziedelth.jais.clients

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.Emoji
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Country
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.News
import fr.ziedelth.jais.utils.clients.Client
import fr.ziedelth.jais.utils.clients.JClient
import fr.ziedelth.jais.utils.tokens.TwitterToken
import twitter4j.*
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
    private val location = GeoLocation(45.764043, 4.835659)
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

    override fun getJClient(): JClient {
        return if (this.file.exists()) Const.GSON.fromJson(
            Files.readString(this.file.toPath(), Const.DEFAULT_CHARSET),
            JClient::class.java
        ) else JClient()
    }

    override fun saveJClient(jClient: JClient) {
        Files.writeString(this.file.toPath(), Const.GSON.toJson(jClient), Const.DEFAULT_CHARSET)
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

    override fun sendNewEpisodes(episodes: Array<Episode>) {
        if (!this.init || episodes.isEmpty()) return
        val jClient = this.getJClient()
        val countryEpisodes = episodes.filter { it.country == Country.FRANCE }

        countryEpisodes.forEach { episode ->
            val jEpisode = jClient.getEpisodeById(Const.toId(episode))

            try {
                val uploadMedia =
                    this.twitter!!.uploadMedia("${Const.toId(episode)}.jpg", downloadImageEpisode(episode))

                val statusMessage = StatusUpdate(
                    "🔜 ${episode.anime}\n${if (episode.title != null) "${episode.title}\n" else ""}" +
                            "${episode.season} • ${episode.country.episode} ${episode.number} ${if (episode.type == EpisodeType.SUBTITLED) episode.country.subtitled else episode.country.dubbed}\n" +
                            "${Emoji.CLAP} ${
                                SimpleDateFormat(
                                    "mm:ss"
                                ).format(episode.duration * 1000)
                            }\n" +
                            "#Anime #${
                                episode.platform.getName().replace(
                                    " ",
                                    ""
                                )
                            }\n\n${episode.url}"
                )
                statusMessage.setMediaIds(uploadMedia.mediaId)
                statusMessage.location = this.location

                val tweet = this.twitter!!.updateStatus(statusMessage)
                jEpisode.messages.add(tweet.id)
                jClient.addEpisode(jEpisode)
            } catch (exception: Exception) {
                JLogger.warning("Can not tweet episodes: ${exception.message ?: "Nothing..."}")
            }
        }

        this.saveJClient(jClient)
    }

    override fun sendEditEpisodes(episodes: Array<Episode>) {}

    private fun downloadImageEpisode(episode: Episode): ByteArrayInputStream {
        val bufferedImage = ImageIO.read(URL(episode.image))
        val baos = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "jpg", baos)
        return ByteArrayInputStream(baos.toByteArray())
    }

    override fun sendNews(news: Array<News>) {
        if (!this.init) return

        news.filter { it.country == Country.FRANCE }.forEach {
            try {
                val statusMessage = StatusUpdate(
                    "${Const.substring(it.title, 100)}...\n#${
                        it.platform.replace(
                            " ",
                            ""
                        )
                    }\n\nLire l'actualité :\n${it.link}"
                )
                statusMessage.location = this.location

                this.twitter!!.updateStatus(statusMessage)
            } catch (twitterException: TwitterException) {
                JLogger.warning("Can not tweet news: ${twitterException.message ?: "Nothing..."}")
            }
        }
    }
}