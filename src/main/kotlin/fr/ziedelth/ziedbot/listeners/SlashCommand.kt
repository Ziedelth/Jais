package fr.ziedelth.ziedbot.listeners

import fr.ziedelth.ziedbot.ZiedBot
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.commands.Command
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction

class SlashCommand(ziedBot: ZiedBot) : ListenerAdapter() {
    init {
        ziedBot.jda.awaitReady()

        ziedBot.jda.guilds.forEach { guild ->
            val commandUpdateAction: CommandUpdateAction = guild.updateCommands()

            Const.commands.forEach { command ->
                run {
                    val commandData: CommandUpdateAction.CommandData =
                        CommandUpdateAction.CommandData(command.name, command.description)
                    command.options.forEach { option ->
                        commandData.addOption(
                            CommandUpdateAction.OptionData(
                                option.type,
                                option.name,
                                option.description
                            ).setRequired(option.required)
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
            Const.commands.first { command -> command.name == commandName && member?.hasPermission(command.permission) == true }
        } catch (exception: NoSuchElementException) {
            null
        }

        if (command != null) command.execute(event) else event.reply("I can't handle that command right now :(")
            .setEphemeral(true).queue()
    }
}