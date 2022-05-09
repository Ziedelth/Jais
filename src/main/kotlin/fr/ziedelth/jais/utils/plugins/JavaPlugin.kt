/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News
import fr.ziedelth.jais.utils.animes.Scan
import org.pf4j.Plugin
import org.pf4j.PluginWrapper

/* Kotlin is a JVM language that allows you to write Java-like code in a more concise way */
abstract class JavaPlugin(wrapper: PluginWrapper?) : Plugin(wrapper) {
    open fun reset() {}
    open fun newEpisode(episode: Episode) {}
    open fun newScan(scan: Scan) {}
    open fun newNews(news: News) {}
    open fun sendMessage(message: String) {}
    abstract fun getFollowers(): Int
}