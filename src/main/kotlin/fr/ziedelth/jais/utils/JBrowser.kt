/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import com.google.common.io.Files
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object JBrowser {
    fun get(url: String?): Document? {
        if (url.isNullOrEmpty()) return null
        val randomCode = UUID.randomUUID().toString().replace("-", "")
        JLogger.config("Opening url: $url")
        val folder = FileImpl.getFile("browser")

        if (!folder.exists()) {
            JLogger.warning("Cannot find browser folder, please install it...")
            return null
        }

        val folderResults = File(folder, "results")
        if (!folderResults.exists()) folderResults.mkdirs()
        val code =
            Runtime.getRuntime().exec("node index.js $url $randomCode", null, folder).waitFor(2L, TimeUnit.MINUTES)

        return if (code) {
            JLogger.config("Saving... ($url)")
            val file = File(folder, "result-$randomCode.html")
            val document = Jsoup.parse(file, "UTF-8", url)
            Files.copy(file, File(folderResults, "result-$randomCode.html"))
            file.delete()
            JLogger.config("Saved! ($url)")
            document
        } else {
            JLogger.warning("Failed to open $url...")
            null
        }
    }
}