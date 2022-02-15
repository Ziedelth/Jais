/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class NotificationsTest {
    @Test
    fun testAdd() {
        Notifications.add("test")
        assertEquals(1, Notifications.map.size)
        assertEquals(0, Notifications.notify.size)
        assertEquals(1, Notifications.send())
        assertEquals(1, Notifications.notify.size)
    }
}