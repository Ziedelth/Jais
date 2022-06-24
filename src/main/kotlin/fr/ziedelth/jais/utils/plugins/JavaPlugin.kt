/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News
import org.pf4j.Plugin
import org.pf4j.PluginWrapper

abstract class JavaPlugin(wrapper: PluginWrapper?) : Plugin(wrapper) {
    open fun reset() {}
    open fun newEpisode(episode: Episode) {}
    open fun newNews(news: News) {}
    open fun sendMessage(message: String) {}
    abstract fun getFollowers(): Int
}