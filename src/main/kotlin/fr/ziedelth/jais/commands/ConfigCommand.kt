/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.commands

import fr.ziedelth.jais.utils.*
import fr.ziedelth.jais.utils.animes.Country
import fr.ziedelth.jais.utils.commands.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

class ConfigCommand : Command(
    "config",
    "Configure your channel to receive anime and news. Only for administrators",
) {
    override fun execute(event: SlashCommandEvent) {
        val user = event.user
        val textChannel = event.textChannel

        if (hasConfiguration(textChannel)) {
            val configurationBuilder = getConfiguration(textChannel)!!

            if (configurationBuilder.user == user) event.reply("Editing...")
                .queue { it -> it.retrieveOriginal().queue { this.sendConfiguration(it, configurationBuilder) } }
            else event.reply("This channel is in configuration, please wait...").setEphemeral(true).queue()
        } else {
            if (event.member!!.hasPermission(Permission.ADMINISTRATOR)) {
                event.reply("Editing...").queue { it ->
                    it.retrieveOriginal().queue {
                        this.sendConfiguration(
                            it,
                            ConfigurationBuilder(
                                user = user,
                                textChannel = textChannel,
                                configurationStep = ConfigurationStep.COUNTRIES
                            )
                        )
                    }
                }
            } else event.reply("You've not permission to use this command!").setEphemeral(true).queue()
        }
    }

    private fun sendConfiguration(message: Message, configurationBuilder: ConfigurationBuilder) {
        addConfiguration(configurationBuilder)
        removeAllReactionsFrom(message)

        message.clearReactions().queue {
            val reactions: MutableList<Reaction> = mutableListOf()

            when (configurationBuilder.configurationStep) {
                ConfigurationStep.COUNTRIES -> {
                    reactions.add(
                        Reaction(
                            message = message,
                            unicode = Emoji.BACKWARD,
                            onClick = object : ClickRunnable {
                                override fun run(clickType: ClickType, user: Long) {
                                    if (user == configurationBuilder.user.idLong && clickType == ClickType.ADD && hasConfiguration(
                                            configurationBuilder
                                        )
                                    ) {
                                        var page = configurationBuilder.page - 1
                                        if (page <= 0) page = 1
                                        configurationBuilder.page = page

                                        message.editMessageEmbeds(getEmbed(configurationBuilder).build()).queue()
                                        addConfiguration(configurationBuilder)
                                    }
                                }
                            })
                    )

                    configurationBuilder.getCountriesPerPage().forEach { country: Country ->
                        reactions.add(
                            Reaction(
                                message = message,
                                unicode = country.flag,
                                onClick = object : ClickRunnable {
                                    override fun run(clickType: ClickType, user: Long) {
                                        if (user == configurationBuilder.user.idLong && hasConfiguration(
                                                configurationBuilder
                                            )
                                        ) {
                                            if (clickType == ClickType.ADD) configurationBuilder.addCountry(country)
                                            else configurationBuilder.removeCountry(country)

                                            message.editMessageEmbeds(getEmbed(configurationBuilder).build()).queue()
                                            addConfiguration(configurationBuilder)
                                        }
                                    }
                                })
                        )
                    }

                    reactions.add(
                        Reaction(
                            message = message,
                            unicode = Emoji.FORWARD,
                            onClick = object : ClickRunnable {
                                override fun run(clickType: ClickType, user: Long) {
                                    if (user == configurationBuilder.user.idLong && clickType == ClickType.ADD && hasConfiguration(
                                            configurationBuilder
                                        )
                                    ) {
                                        var page = configurationBuilder.page + 1
                                        if (page >= configurationBuilder.getMaxPagesDisplay()) page =
                                            configurationBuilder.getMaxPagesDisplay()
                                        configurationBuilder.page = page

                                        message.editMessageEmbeds(getEmbed(configurationBuilder).build()).queue()
                                        addConfiguration(configurationBuilder)
                                    }
                                }
                            })
                    )

                    reactions.add(Reaction(message = message, unicode = Emoji.CHECK, onClick = object : ClickRunnable {
                        override fun run(clickType: ClickType, user: Long) {
                            if (user == configurationBuilder.user.idLong && clickType == ClickType.ADD && hasConfiguration(
                                    configurationBuilder
                                )
                            ) {
                                configurationBuilder.nextStep()
                                this@ConfigCommand.sendConfiguration(message, configurationBuilder)
                            }
                        }
                    }))
                }

                ConfigurationStep.ANIME -> {
                    reactions.add(Reaction(message = message, unicode = Emoji.CHECK, onClick = object : ClickRunnable {
                        override fun run(clickType: ClickType, user: Long) {
                            if (user == configurationBuilder.user.idLong && clickType == ClickType.ADD && hasConfiguration(
                                    configurationBuilder
                                )
                            ) {
                                configurationBuilder.anime = true
                                configurationBuilder.nextStep()
                                this@ConfigCommand.sendConfiguration(message, configurationBuilder)
                            }
                        }
                    }))

                    reactions.add(Reaction(message = message, unicode = Emoji.NO, onClick = object : ClickRunnable {
                        override fun run(clickType: ClickType, user: Long) {
                            if (user == configurationBuilder.user.idLong && clickType == ClickType.ADD && hasConfiguration(
                                    configurationBuilder
                                )
                            ) {
                                configurationBuilder.anime = false
                                configurationBuilder.nextStep()
                                this@ConfigCommand.sendConfiguration(message, configurationBuilder)
                            }
                        }
                    }))
                }

                ConfigurationStep.NEWS -> {
                    reactions.add(Reaction(message = message, unicode = Emoji.CHECK, onClick = object : ClickRunnable {
                        override fun run(clickType: ClickType, user: Long) {
                            if (user == configurationBuilder.user.idLong && clickType == ClickType.ADD && hasConfiguration(
                                    configurationBuilder
                                )
                            ) {
                                configurationBuilder.news = true
                                configurationBuilder.nextStep()
                                this@ConfigCommand.sendConfiguration(message, configurationBuilder)
                            }
                        }
                    }))

                    reactions.add(Reaction(message = message, unicode = Emoji.NO, onClick = object : ClickRunnable {
                        override fun run(clickType: ClickType, user: Long) {
                            if (user == configurationBuilder.user.idLong && clickType == ClickType.ADD && hasConfiguration(
                                    configurationBuilder
                                )
                            ) {
                                configurationBuilder.news = false
                                configurationBuilder.nextStep()
                                this@ConfigCommand.sendConfiguration(message, configurationBuilder)
                            }
                        }
                    }))
                }

                ConfigurationStep.FINISH -> {
                    reactions.add(Reaction(message = message, unicode = Emoji.CHECK, onClick = object : ClickRunnable {
                        override fun run(clickType: ClickType, user: Long) {
                            if (user == configurationBuilder.user.idLong && clickType == ClickType.ADD && hasConfiguration(
                                    configurationBuilder
                                )
                            ) {
                                message.delete().queue()

                                removeAllReactionsFrom(message)
                                removeConfiguration(configurationBuilder.textChannel)

                                val jGuild = configurationBuilder.textChannel.guild.getJGuild()
                                val channel = jGuild.animeChannels[configurationBuilder.textChannel.id] ?: Channel()
                                channel.anime = configurationBuilder.anime
                                channel.news = configurationBuilder.news
                                channel.addAllCountries(configurationBuilder.countries)
                                jGuild.animeChannels[configurationBuilder.textChannel.id] = channel
                                jGuild.save()
                            }
                        }
                    }))

                    reactions.add(Reaction(message = message, unicode = Emoji.NO, onClick = object : ClickRunnable {
                        override fun run(clickType: ClickType, user: Long) {
                            if (user == configurationBuilder.user.idLong && clickType == ClickType.ADD && hasConfiguration(
                                    configurationBuilder
                                )
                            ) {
                                configurationBuilder.configurationStep = ConfigurationStep.COUNTRIES
                                this@ConfigCommand.sendConfiguration(message, configurationBuilder)
                            }
                        }
                    }))
                }

                else -> {

                }
            }

            // DELETE
            reactions.add(Reaction(message = message, unicode = Emoji.TRASH_CAN, onClick = object : ClickRunnable {
                override fun run(clickType: ClickType, user: Long) {
                    if (user == configurationBuilder.user.idLong && clickType == ClickType.ADD && hasConfiguration(
                            configurationBuilder
                        )
                    ) {
                        message.delete().queue()

                        removeAllReactionsFrom(message)
                        removeConfiguration(configurationBuilder.textChannel)
                    }
                }
            }))

            message.editMessageEmbeds(getEmbed(configurationBuilder).build())
                .queue { this.sendReaction(reactions.toTypedArray(), 0) }
        }
    }

    private fun sendReaction(reactions: Array<Reaction>, index: Int) {
        reactions.getOrNull(index)?.add { this.sendReaction(reactions, index + 1) }
    }

    private fun getEmbed(configurationBuilder: ConfigurationBuilder?): EmbedBuilder {
        val embed = EmbedBuilder()
        embed.setColor(Const.MAIN_COLOR)

        when (configurationBuilder?.configurationStep) {
            ConfigurationStep.COUNTRIES -> {
                embed.setTitle("Configuration\n→ Available countries")
                val countriesString = StringBuilder()
                val countries = configurationBuilder.getCountriesPerPage()

                countries.forEach {
                    countriesString.append(
                        "• ${if (configurationBuilder.countries.contains(it)) "~~" else ""}${it.flag} ${it.countryName}${
                            if (configurationBuilder.countries.contains(
                                    it
                                )
                            ) "~~" else ""
                        }"
                    ).append("\n")
                }

                embed.setDescription("Select the countries on which you want to receive the anime\n\n$countriesString")
                embed.setFooter("Page ${configurationBuilder.page} / ${configurationBuilder.getMaxPagesDisplay()}")
            }

            ConfigurationStep.ANIME -> {
                embed.setTitle("Configuration\n→ Anime")
                embed.setDescription("Select if you want to receive the anime\n\n• ${Emoji.CHECK} Yes\n• ${Emoji.NO} No")
            }

            ConfigurationStep.NEWS -> {
                embed.setTitle("Configuration\n→ News")
                embed.setDescription("Select if you want to receive the news\n\n• ${Emoji.CHECK} Yes\n• ${Emoji.NO} No")
            }

            ConfigurationStep.FINISH -> {
                embed.setTitle("Configuration\n→ Finished?")
                val countriesString = StringBuilder()
                configurationBuilder.countries.forEach {
                    countriesString.append("• ${it.flag} ${it.countryName}").append("\n")
                }
                embed.setDescription("Do you confirm this configuration?\n\n**→ COUNTRIES ←**\n$countriesString\n**→ SETTINGS ←**\n• Anime: **${configurationBuilder.anime}**\n• News: **${configurationBuilder.news}**")
            }

            else -> {
                embed.setTitle("Configuration\n→ Unknown state")
                embed.setDescription("Bip boup bip bip... ?!?")
            }
        }

        return embed
    }
}