/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils

import org.junit.Test
import kotlin.test.assertEquals

internal class ISO8601Test {
    @Test
    fun testFromCalendarADN() {
        val test = "2022-01-27T15:00:00Z"
        assertEquals("2022-01-27T15:00:00Z", ISO8601.toUTCDate(ISO8601.fromCalendar1(test)))
    }

    @Test
    fun testFromCalendarCrunchyroll() {
        val test = "Thu, 27 Jan 2022 17:25:00 GMT"
        assertEquals("2022-01-27T18:25:00+01:00", ISO8601.fromCalendar2(test))
        assertEquals("2022-01-27T17:25:00Z", ISO8601.toUTCDate(ISO8601.fromCalendar2(test)))
    }

    @Test
    fun testFromCalendarScantrad() {
        val test = "Thu, 27 Jan 2022 14:01:19 +0100"
        assertEquals("2022-01-27T14:01:19+01:00", ISO8601.fromCalendar2(test))
        assertEquals("2022-01-27T13:01:19Z", ISO8601.toUTCDate(ISO8601.fromCalendar2(test)))
    }
}