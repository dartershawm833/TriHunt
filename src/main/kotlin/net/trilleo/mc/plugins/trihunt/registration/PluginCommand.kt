package net.trilleo.mc.plugins.trihunt.registration

import org.bukkit.command.CommandSender

/**
 * Base class for all plugin commands.
 *
 * Extend this class and place the subclass anywhere inside the
 * `net.trilleo.mc.plugins.trihunt.commands` package (or any subpackage) to
 * have it automatically discovered and registered at startup.
 *
 * The class must have either:
 * - A no-arg constructor, **or**
 * - A constructor that accepts a single `JavaPlugin` parameter (the plugin
 *   instance will be injected automatically).
 *
 * Example:
 * ```kotlin
 * package net.trilleo.mc.plugins.trihunt.commands.game
 *
 * class StartCommand : PluginCommand(
 *     name = "start",
 *     description = "Start the manhunt",
 *     usage = "/start",
 *     aliases = listOf("begin"),
 *     permission = "trihunt.start"
 * ) {
 *     override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
 *         sender.sendMessage("The hunt has begun!")
 *         return true
 *     }
 * }
 * ```
 */
abstract class PluginCommand(
    val name: String,
    val description: String = "",
    val usage: String = "/<command>",
    val aliases: List<String> = emptyList(),
    val permission: String? = null
) {

    /**
     * Called when the command is executed.
     *
     * @param sender the entity that issued the command
     * @param args   the arguments passed after the command name
     * @return `true` if the command was handled successfully
     */
    abstract fun execute(sender: CommandSender, args: Array<out String>): Boolean

    /**
     * Called when tab-completion is requested for this command.
     *
     * @param sender the entity requesting completions
     * @param args   the arguments typed so far
     * @return a list of possible completions, or an empty list
     */
    open fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> = emptyList()
}
