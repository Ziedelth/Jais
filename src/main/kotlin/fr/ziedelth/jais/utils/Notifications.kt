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
    private val map: MutableList<String> = mutableListOf()
    private var notify: MutableList<String> = mutableListOf()

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
    }

    fun clear() {
        this.map.clear()
        this.notify.clear()
    }

    fun add(anime: String) {
        val code = HashUtils.sha512(anime.lowercase().onlyLettersAndDigits())

        if (this.map.contains(code))
            return

        this.map.add(code)
    }

    fun send() {
        val notContains = this.map.filter { !this.notify.contains(it) }
        this.notify = this.map

        FirebaseMessaging.getInstance().send(
            Message.builder().setAndroidConfig(
                AndroidConfig.builder().setNotification(
                    AndroidNotification.builder().setTitle(if (notContains.size > 1) "Nouvelles sorties" else "Nouvelle sortie").setBody(notContains.joinToString(", ")).build()
                ).build()
            ).setTopic("animes").build()
        )
    }
}