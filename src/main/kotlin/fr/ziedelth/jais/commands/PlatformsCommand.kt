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
        val platformsBuilder = StringBuilder()

        Const.PLATFORMS.forEach { platform ->
            val flagBuilder = StringBuilder()
            platform.getAllowedCountries().forEach { flagBuilder.append(it.flag).append(" ") }
            platformsBuilder.append("• $flagBuilder [${platform.getName()}](${platform.getURL()})").append("\n")
        }

        embed.setDescription(
            """** Social networks **
            • Discord (Here)
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