package fr.ziedelth.jais.listeners

import fr.ziedelth.jais.utils.ClickType
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.reactions
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildMessageReactionAdd : ListenerAdapter() {
    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        super.onGuildMessageReactionAdd(event)
        if (event.user.isBot) return

        val id = Const.encodeMD5("${event.messageIdLong}${event.reactionEmote.asReactionCode}".toByteArray())
        if (reactions.containsKey(id)) reactions[id]!!.onClick.run(reactions[id]!!, ClickType.ADD, event.userIdLong)
    }
}