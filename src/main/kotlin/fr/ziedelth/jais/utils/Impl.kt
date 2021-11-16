/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import fr.ziedelth.jais.utils.debug.JLogger
import java.util.logging.Level

object Impl {
    fun tryCatch(errorMessage: String? = null, action: () -> Unit) {
        try {
            action.invoke()
        } catch (exception: Exception) {
            if (!errorMessage.isNullOrEmpty()) JLogger.log(Level.WARNING, errorMessage, exception)
        }
    }
}