/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais.utils

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.Scan
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.plugins.PluginUtils.onlyLettersAndDigits
import java.io.FileInputStream

object Notifications {
    private var init = false
    val map: MutableMap<String, String> = mutableMapOf()
    val notify: MutableMap<String, String> = mutableMapOf()

    fun init() {
        JLogger.info("Setup notifications...")
        val file = FileImpl.getFile("firebase_key.json")

        if (!file.exists()) {
            JLogger.warning("Notifications file not found, ignoring...")
            return
        }

        FirebaseApp.initializeApp(
            FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(FileInputStream(file)))
                .setProjectId("866259759032").build()
        )

        init = true
    }

    fun clear() {
        this.map.clear()
        this.notify.clear()
    }

    fun add(anime: String) {
        val code = HashUtils.sha512(anime.lowercase().onlyLettersAndDigits())

        if (this.map.containsKey(code))
            return

        this.map[code] = anime
    }

    fun send(): Int {
        val notContains = this.map.filter { entry -> !this.notify.containsKey(entry.key) }

        if (notContains.isEmpty())
            return 0

        this.notify.putAll(notContains)

        if (!this.init)
            return notContains.size

        FirebaseMessaging.getInstance().send(
            Message.builder().setAndroidConfig(
                AndroidConfig.builder().setNotification(
                    AndroidNotification.builder().setTitle(if (notContains.size > 1) "Nouvelles sorties" else "Nouvelle sortie").setBody(notContains.values.joinToString(", ")).build()
                ).build()
            ).setTopic("animes").build()
        )

        return notContains.size
    }
}