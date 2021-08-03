package fr.ziedelth.jais.utils

import net.dv8tion.jda.api.entities.Message

val reactions: MutableMap<String, Reaction> = mutableMapOf()

fun removeAllReactionsFrom(message: Message) {
    val ids: MutableList<String> = mutableListOf()
    reactions.filter { (_, reaction) -> reaction.message.idLong == message.idLong }.forEach { (id, _) -> ids.add(id) }
    ids.forEach { reactions.remove(it) }
}

fun removeAllDeprecatedReactions() {
    val ids: MutableList<String> = mutableListOf()
    reactions.filter { (_, reaction) -> (System.currentTimeMillis() - reaction.timestamp) >= 3600000L }
        .forEach { (id, _) -> ids.add(id) }
    ids.forEach { reactions.remove(it) }
}

class Reaction(
    val timestamp: Long = System.currentTimeMillis(),
    val message: Message,
    val unicode: String,
    val onClick: ClickRunnable
) {
    val id = Const.encodeMD5("${this.message.idLong}${this.unicode}".toByteArray())

    fun add(action: Runnable? = null) {
        if (!reactions.containsKey(this.id)) {
            reactions[this.id] = this
            this.message.addReaction(this.unicode).queue { action?.run() }
        }
    }

    fun remove() {
        if (reactions.containsKey(this.id)) {
            this.message.removeReaction(this.unicode).queue()
            reactions.remove(this.id)
        }
    }
}