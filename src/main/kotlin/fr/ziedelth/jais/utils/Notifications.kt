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
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.plugins.PluginUtils.onlyLettersAndDigits
import java.io.FileInputStream

object Notifications {
    private val map = mutableMapOf<Country, MutableList<String>>()

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
    }

    fun notifyEpisode(episode: Episode) {
        val list = this.map.getOrDefault(episode.country.country, mutableListOf())
        val code = HashUtils.sha512(episode.anime.lowercase().onlyLettersAndDigits())

        if (list.contains(code))
            return

        list.add(code)
        this.map[episode.country.country] = list

        FirebaseMessaging.getInstance().send(
            Message.builder().setAndroidConfig(
                AndroidConfig.builder().setNotification(
                    AndroidNotification.builder().setTitle("Nouvelles sorties").setBody(episode.anime).build()
                ).build()
            ).setTopic("animes").build()
        )
    }
}