/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ImplTest {
    @Test
    fun testHasInternet() {
        assertEquals(true, Impl.hasInternet())
    }
}