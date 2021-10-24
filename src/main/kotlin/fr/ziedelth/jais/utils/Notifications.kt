/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.debug.JLogger
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.net.URL
import javax.imageio.ImageIO

object Notifications {
    private val systemTray: SystemTray?
    private val trayIcon: TrayIcon?

    init {
        if (SystemTray.isSupported()) {
            this.systemTray = SystemTray.getSystemTray()
            val image = ImageIO.read(URL("https://dev.ziedelth.fr/images/jais.jpg"))
            this.trayIcon = TrayIcon(image, "Jaïs", PopupMenu("Jaïs"))
            this.trayIcon.isImageAutoSize = true
            this.trayIcon.toolTip = "Jaïs"

            this.systemTray.add(this.trayIcon)
        } else {
            this.systemTray = null
            this.trayIcon = null
            JLogger.warning("Notifications system is not available on this OS!")
        }
    }

    fun sendNotification(title: String, description: String) =
        this.trayIcon?.displayMessage(title, description, TrayIcon.MessageType.NONE)

    fun sendEpisodeNotification(episode: Episode) = this.sendNotification(episode.anime, "New episode available!")
}