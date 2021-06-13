package fr.ziedelth.ziedbot.platforms

import fr.ziedelth.ziedbot.utils.animes.Episode
import fr.ziedelth.ziedbot.utils.animes.News
import fr.ziedelth.ziedbot.utils.animes.Platform
import java.awt.Color

class Wakanim : Platform {
    override fun getPrefix(): String = "WAK-E"
    override fun getName(): String = "Wakanim"
    override fun getURL(): String = "https://www.wakanim.tv/"
    override fun getImage(): String =
        "https://play-lh.googleusercontent.com/J5_U63e4nJPrSUHeqqGIoZIaqQ1EYKEeXpcNaVbf95adUu9O9VnEgXC_ejUZPaCjpw"

    override fun getColor(): Color = Color(227, 71, 75)

    override fun getLastNews(): Array<News> = arrayListOf<News>().toTypedArray()
    override fun getLastEpisodes(): Array<Episode> = arrayListOf<Episode>().toTypedArray()

    override fun toString(): String = this.getName()
}