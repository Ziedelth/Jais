/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.commands

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.commands.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

class PlatformsCommand : Command(
    "platforms",
    "Get all platforms used by Jaïs",
) {
    override fun execute(event: SlashCommandEvent) {
        val embed = EmbedBuilder()
        val platformsBuilder = Const.PLATFORMS.map { platform ->
            "• ${
                platform.getAllowedCountries().map { "${it.flag} " }
            } [${platform.getName()}](${platform.getURL()})\n"
        }.toString()

        embed.setDescription(
            """** Social networks **
            • Discord (Here)
            • [Instagram](https://www.instagram.com/jais_zie/)
            • [Twitter](https://twitter.com/Jaiss_B_)
            • [GitHub](https://github.com/Ziedelth/Jais)
            
            ** Platforms to check episodes **
            $platformsBuilder
        """.trimIndent()
        )
        embed.setColor(Const.MAIN_COLOR)

        event.reply("Platforms :").addEmbeds(embed.build()).setEphemeral(true).queue()
    }
}