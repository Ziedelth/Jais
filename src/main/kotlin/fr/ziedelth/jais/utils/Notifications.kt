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
import java.io.FileInputStream

data class Anime(val id: Long, val name: String)

object Notifications {
    private var init = false
    val map = mutableListOf<Anime>()
    val notify = mutableListOf<Anime>()

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

    fun add(anime: Anime) {
        if (this.map.any { it.id == anime.id })
            return

        this.map.add(anime)
    }

    fun send(): Int {
        val notContains = this.map.filter { a -> !this.notify.any { a.id == it.id } }

        if (notContains.isEmpty())
            return 0

        this.notify.addAll(notContains)

        if (!this.init)
            return notContains.size

        sendNotifications(notContains)
        return notContains.size
    }

    private fun notification(title: String, body: String, topic: String) {
        FirebaseMessaging.getInstance().send(
            Message.builder().setAndroidConfig(
                AndroidConfig.builder().setNotification(
                    AndroidNotification.builder()
                        .setTitle(title)
                        .setBody(body).build()
                ).build()
            ).setTopic(topic).build()
        )
    }

    private fun sendNotifications(list: Collection<Anime>) {
        notification(if (list.size > 1) "Nouvelles sorties" else "Nouvelle sortie", list.joinToString(", ") { it.name }, "animes")
        list.forEach { notification("Nouvelle sortie", it.name, it.id.toString()) }
    }
}