/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.countries

import fr.ziedelth.jais.utils.animes.platforms.Platform

interface Country {
    fun checkOnNewsURL(platform: Platform?): String?
    fun checkOnEpisodesURL(platform: Platform?): String?
    fun restrictionEpisodes(platform: Platform?): String?
    fun subtitlesEpisodes(platform: Platform?): String?
    fun dubbedEpisodes(platform: Platform?): String?
}