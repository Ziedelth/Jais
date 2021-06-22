package fr.ziedelth.ziedbot.listeners

import fr.ziedelth.ziedbot.utils.ZiedLogger
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildMessageReactionAdd : ListenerAdapter() {
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        super.onGuildMessageReceived(event)

        if (event.author.isBot) return

        val message = event.message
        val content = message.contentRaw

        if (content.isNotEmpty()) ZiedLogger.info("${event.author.asTag} send message: $content")
        else ZiedLogger.info("${event.author.asTag} send file(s)")
    }
}