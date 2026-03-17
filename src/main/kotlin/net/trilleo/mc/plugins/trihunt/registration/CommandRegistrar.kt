package net.trilleo.mc.plugins.trihunt.registration

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

/**
 * Discovers all concrete [PluginCommand] subclasses inside the `commands`
 * package (and its subpackages) and registers them with the server's
 * [CommandMap] so they are available in-game without manual `plugin.yml`
 * entries.
 */
object CommandRegistrar {

    private const val COMMANDS_PACKAGE = "net.trilleo.mc.plugins.trihunt.commands"

    /**
     * Scans the commands package, instantiates every [PluginCommand] found,
     * and registers it on the server command map.
     */
    fun registerAll(plugin: JavaPlugin) {
        val commandClasses = PackageScanner.findClasses(
            plugin, COMMANDS_PACKAGE, PluginCommand::class.java
        )
        val commandMap = getCommandMap()

        for (commandClass in commandClasses) {
            try {
                val command = instantiate(commandClass, plugin)
                val bukkitCommand = createBukkitCommand(command)
                commandMap.register(plugin.name.lowercase(), bukkitCommand)
                plugin.logger.info("Registered command: /${command.name}")
            } catch (e: Exception) {
                plugin.logger.severe(
                    "Failed to register command ${commandClass.simpleName}: ${e.message}"
                )
            }
        }

        plugin.logger.info("Registered ${commandClasses.size} command(s)")
    }

    /** Resolves the server [CommandMap] via reflection for broad compatibility. */
    private fun getCommandMap(): CommandMap {
        val server = Bukkit.getServer()
        val method = server.javaClass.getMethod("getCommandMap")
        return method.invoke(server) as CommandMap
    }

    /**
     * Tries to create an instance of [clazz] using a constructor that accepts
     * a [JavaPlugin]; falls back to a no-arg constructor.
     */
    private fun instantiate(clazz: Class<out PluginCommand>, plugin: JavaPlugin): PluginCommand {
        return try {
            clazz.getDeclaredConstructor(JavaPlugin::class.java).newInstance(plugin)
        } catch (_: NoSuchMethodException) {
            try {
                clazz.getDeclaredConstructor().newInstance()
            } catch (_: NoSuchMethodException) {
                throw IllegalArgumentException(
                    "${clazz.simpleName} must declare either a no-arg constructor " +
                        "or a constructor accepting a single JavaPlugin parameter"
                )
            }
        }
    }

    /** Wraps a [PluginCommand] in a Bukkit [Command] suitable for the command map. */
    private fun createBukkitCommand(command: PluginCommand): Command {
        return object : Command(
            command.name,
            command.description,
            command.usage,
            command.aliases
        ) {
            init {
                command.permission?.let { permission = it }
            }

            override fun execute(
                sender: CommandSender,
                commandLabel: String,
                args: Array<out String>
            ): Boolean = command.execute(sender, args)

            override fun tabComplete(
                sender: CommandSender,
                alias: String,
                args: Array<out String>
            ): List<String> = command.tabComplete(sender, args)
        }
    }
}
