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
    private val notify = mutableListOf<Anime>()

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

    data class Notif(val title: String, val body: String, val topic: String)

    private fun notification(notifs: Iterable<Notif>) {
        FirebaseMessaging.getInstance().sendAll(
            notifs.map {
                Message.builder().setAndroidConfig(
                    AndroidConfig.builder().setNotification(
                        AndroidNotification.builder()
                            .setTitle(it.title)
                            .setBody(it.body).build()
                    ).build()
                ).setTopic(it.topic).build()
            }
        )
    }

    private fun sendNotifications(list: Collection<Anime>) {
        val notifs = mutableListOf(
            Notif(
                if (list.size > 1) "Nouvelles sorties" else "Nouvelle sortie",
                list.joinToString(", ") { it.name },
                "animes"
            )
        )
        notifs.addAll(list.map { Notif("Nouvelle sortie", it.name, it.id.toString()) })
        notification(notifs)
    }
}