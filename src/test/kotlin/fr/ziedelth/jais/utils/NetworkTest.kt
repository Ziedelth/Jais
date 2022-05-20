package fr.ziedelth.jais.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class NetworkTest {
    @Test
    fun connect() {
        val networkResponse = Network.connect("https://google.fr/")
        assertEquals(true, networkResponse.isSuccess)
    }
}