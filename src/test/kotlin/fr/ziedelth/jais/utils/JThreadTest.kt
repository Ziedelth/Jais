/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class JThreadTest {
    @Test
    fun testStartMultiThread() {
        val actions: Array<() -> Unit> = arrayOf({ 1 + 1 }, { 2 + 1 }, { 3 + 1 })
        val tested = JThread.startMultiThread(*actions)
        JThread.stopAll()

        assertEquals(actions.size, tested.size)
    }
}