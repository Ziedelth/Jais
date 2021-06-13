package fr.ziedelth.ziedbot.listeners

import fr.ziedelth.ziedbot.ZiedBot
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.commands.Command
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

class SlashCommand(ziedBot: ZiedBot) : ListenerAdapter() {
    init {
        ziedBot.jda.awaitReady()

        ziedBot.jda.guilds.forEach { guild ->
            val commandUpdateAction: CommandListUpdateAction = guild.updateCommands()

            Const.COMMANDS.forEach { command ->
                run {
                    val commandData = CommandData(command.name, command.description)
                    command.options.forEach { option ->
                        commandData.addOption(
                            option.type,
                            option.name,
                            option.description,
                            option.required
                        )
                    }
                    commandUpdateAction.addCommands(commandData)
                }
            }

            commandUpdateAction.submit()
        }
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        super.onSlashCommand(event)

        if (event.guild == null) return

        val member: Member? = event.member
        val commandName: String = event.name

        val command: Command? = try {
            Const.COMMANDS.first { command -> command.name == commandName && member?.hasPermission(command.permission) == true }
        } catch (exception: NoSuchElementException) {
            null
        }

        if (command != null) command.execute(event) else event.reply("I can't handle that command right now :(")
            .setEphemeral(true).queue()
    }

    override fun onButtonClick(event: ButtonClickEvent) {
        super.onButtonClick(event)

        if (event.componentId == "test") {
            event.reply("Hello :)").addActionRow(Button.success("test", "TEST"), Button.danger("error", "ERROR"))
                .queue()
        } else {
            event.editMessage("Error...").queue()
        }
    }
}