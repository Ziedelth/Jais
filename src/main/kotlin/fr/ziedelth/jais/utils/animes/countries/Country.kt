/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.countries

import fr.ziedelth.jais.utils.animes.platforms.Platform

interface Country {
    /**
     * "Check on the news URL of the given platform."
     *
     * The function is a function that takes a platform as a parameter and returns a string
     *
     * @param platform Platform?
     */
    fun checkOnNewsURL(platform: Platform?): String?

    /**
     * "Check on the episodes URL of the given platform."
     *
     * The function's name is `checkOnEpisodesURL`. It takes a single parameter, `platform`, which is a `Platform` object
     *
     * @param platform The platform to check.
     */
    fun checkOnEpisodesURL(platform: Platform?): String?

    /**
     * Return the number of episodes of the show on the given platform.
     *
     * @param platform The platform to restrict the episodes to.
     */
    fun restrictionEpisodes(platform: Platform?): String?

    /**
     * Get the subtitles for the episodes of a show.
     *
     * @param platform Platform?
     */
    fun subtitlesEpisodes(platform: Platform?): String?

    /**
     * Return the number of episodes in the series that have been dubbed into the given language.
     *
     * @param platform The platform to get the dubbed episodes for.
     */
    fun dubbedEpisodes(platform: Platform?): String?
}