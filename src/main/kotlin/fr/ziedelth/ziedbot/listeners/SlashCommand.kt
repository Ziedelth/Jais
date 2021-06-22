package fr.ziedelth.ziedbot.listeners

import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.commands.Command
import fr.ziedelth.ziedbot.utils.commands.buttons
import fr.ziedelth.ziedbot.utils.guilds
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

class SlashCommand : ListenerAdapter() {
    init {
        guilds.forEach { (_, ziedGuild) ->
            val commandUpdateAction: CommandListUpdateAction = ziedGuild.guild.updateCommands()

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

        if (buttons.containsKey(event.componentId)) {
            buttons[event.componentId]?.consumer?.accept(event)
            buttons.remove(event.componentId)
            return
        }

        event.deferReply().queue()
        event.hook.sendMessage("No actions to this button").queue()
    }
}