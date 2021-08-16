/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.listeners

import fr.ziedelth.jais.utils.JLogger
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildMessageReceived : ListenerAdapter() {
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        super.onGuildMessageReceived(event)

        if (event.author.isBot) return

        val guild = event.guild
        val message = event.message
        val content = message.contentRaw

        if (content.isNotEmpty()) JLogger.info("${event.author.asTag} send message on ${guild.name}: $content")
        else JLogger.info("${event.author.asTag} send file(s) on ${guild.name}")
    }
}