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
import fr.ziedelth.jais.utils.plugins.PluginUtils.onlyLettersAndDigits
import java.io.FileInputStream

/* The `object` keyword is used to create a singleton object. */
object Notifications {
    /* A flag to check if the Firebase SDK has been initialized. */
    private var init = false
    /* It creates a mutable map of String to String. */
    val map: MutableMap<String, String> = mutableMapOf()
    /* It creates a mutable map of String to String. */
    val notify: MutableMap<String, String> = mutableMapOf()

    /**
     * Initialize the Firebase SDK
     */
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

    /**
     * It clears the map and the list of observers
     */
    fun clear() {
        this.map.clear()
        this.notify.clear()
    }

    /**
     * Add a new anime to the database
     *
     * @param anime The name of the anime.
     */
    fun add(anime: String) {
        val code = HashUtils.sha512(anime.lowercase().onlyLettersAndDigits())

        if (this.map.containsKey(code))
            return

        this.map[code] = anime
    }

    /**
     * Send notifications to the user if there are new animes
     *
     * @return The number of notifications sent.
     */
    fun send(): Int {
        val notContains = this.map.filter { entry -> !this.notify.containsKey(entry.key) }

        if (notContains.isEmpty())
            return 0

        this.notify.putAll(notContains)

        if (!this.init)
            return notContains.size

        sendNotifications(notContains.values, "animes")
        notContains.forEach { (entry, value) -> sendNotifications(listOf(value), entry) }

        return notContains.size
    }

    /**
     * Send a notification to a topic
     *
     * @param list Collection<String>
     * @param topic The topic to which the message should be sent.
     */
    private fun sendNotifications(list: Collection<String>, topic: String) {
        FirebaseMessaging.getInstance().send(
            Message.builder().setAndroidConfig(
                AndroidConfig.builder().setNotification(
                    AndroidNotification.builder()
                        .setTitle(if (list.size > 1) "Nouvelles sorties" else "Nouvelle sortie")
                        .setBody(list.joinToString(", ")).build()
                ).build()
            ).setTopic(topic).build()
        )
    }
}