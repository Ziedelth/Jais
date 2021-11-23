/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import fr.ziedelth.jais.utils.debug.JLogger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

object JBrowser {
    fun get(url: String?): Document? {
        if (url.isNullOrEmpty()) return null
        JLogger.info("Opening browser to $url...")
        val folder = File("browser")

        val code = Runtime.getRuntime().exec("node index.js $url", null, folder).waitFor()

        return if (code == 0) {
            JLogger.info("Saving...")
            Jsoup.parse(File(folder, "result.html"), "UTF-8", url)
        } else null
    }
}