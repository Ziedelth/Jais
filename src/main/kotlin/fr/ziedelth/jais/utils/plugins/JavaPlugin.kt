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
    /**
     * Reset the game
     */
    open fun reset() {}

    /**
     * "Open function that takes an Episode parameter and does nothing."
     *
     * The open modifier on the function declaration makes it possible to override the function in a subclass
     *
     * @param episode The episode that was just downloaded.
     */
    open fun newEpisode(episode: Episode) {}

    /**
     * "Create a new scan."
     *
     * The function is open, which means that it can be overridden in a subclass
     *
     * @param scan The scan object that is being processed.
     */
    open fun newScan(scan: Scan) {}

    /**
     * Create new news item
     *
     * @param news News - The news object that was created.
     */
    open fun newNews(news: News) {}

    /**
     * "Get the number of followers."
     *
     * /*
     * Kotlin
     * */
     * abstract fun getFollowing(): Int
     */
    abstract fun getFollowers(): Int
}