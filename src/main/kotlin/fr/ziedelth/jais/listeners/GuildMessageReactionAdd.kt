package fr.ziedelth.jais.listeners

import fr.ziedelth.jais.utils.JLogger
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildMessageReactionAdd : ListenerAdapter() {
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        super.onGuildMessageReceived(event)

        if (event.author.isBot) return

        val message = event.message
        val content = message.contentRaw

        if (content.isNotEmpty()) JLogger.info("${event.author.asTag} send message: $content")
        else JLogger.info("${event.author.asTag} send file(s)")
    }
}