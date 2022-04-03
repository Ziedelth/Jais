/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.commands

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandHandler(
    val command: String,
    val description: String = "No description",
)
