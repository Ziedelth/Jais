package fr.ziedelth.jais.listeners

import fr.ziedelth.jais.utils.JLogger
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildLeave : ListenerAdapter() {
    override fun onGuildLeave(event: GuildLeaveEvent) {
        super.onGuildLeave(event)
        val guild = event.guild

        JLogger.info("I've leave a guild! (${guild.name})")
    }
}