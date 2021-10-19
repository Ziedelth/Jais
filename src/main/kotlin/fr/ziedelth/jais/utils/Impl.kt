/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import java.util.logging.Level

object Impl {
    fun tryCatch(errorMessage: String, action: () -> Unit) {
        try {
            action.invoke()
        } catch (exception: Exception) {
            JLogger.log(Level.WARNING, errorMessage, exception)
        }
    }
}