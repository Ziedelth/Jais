/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.animes.countries.Country

/**
 * Kotlin's data classes are a way to create classes that are immutable and have a single constructor.
 */
data class EpisodeDataImpl(val clazz: Class<out Country>, val data: String)
