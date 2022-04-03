/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.platforms.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class JaisTest {
    private val jais = Jais()

    /* This is a test for the function `addCountry` of the class `Jais`. */
    @Test
    fun testAddCountry() {
        assertEquals(true, this.jais.addCountry(FranceCountry::class.java))
    }

    /* A test for the function `addPlatform` of the class `Jais`. */
    @Test
    fun testAddPlatform() {
        assertEquals(true, this.jais.addPlatform(AnimeDigitalNetworkPlatform::class.java))
        assertEquals(true, this.jais.addPlatform(CrunchyrollPlatform::class.java))
        assertEquals(true, this.jais.addPlatform(NetflixPlatform::class.java))
        assertEquals(true, this.jais.addPlatform(ScantradPlatform::class.java))
        assertEquals(true, this.jais.addPlatform(WakanimPlatform::class.java))
    }
}