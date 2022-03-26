/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils

import org.junit.Test
import kotlin.test.assertEquals

internal class ISO8601Test {
    /* It's a test. */
    @Test
    fun testFromCalendarADN() {
        val test = "2022-01-27T15:00:00Z"
        assertEquals("2022-01-27T15:00:00Z", ISO8601.toUTCDate(ISO8601.fromCalendar1(test)))
    }

    /* It's a test. */
    @Test
    fun testFromCalendarCrunchyroll() {
        val test = "Thu, 27 Jan 2022 17:25:00 GMT"
        assertEquals("2022-01-27T17:25:00Z", ISO8601.toUTCDate(ISO8601.fromCalendar2(test)))
    }

    /* It's a test. */
    @Test
    fun testFromCalendarScantrad() {
        val test = "Thu, 27 Jan 2022 14:01:19 +0100"
        assertEquals("2022-01-27T13:01:19Z", ISO8601.toUTCDate(ISO8601.fromCalendar2(test)))
    }
}