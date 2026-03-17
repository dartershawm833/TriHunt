package net.trilleo.mc.plugins.trihunt

import net.trilleo.mc.plugins.trihunt.registration.CommandRegistrar
import net.trilleo.mc.plugins.trihunt.registration.ListenerRegistrar
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        // Register commands and listeners
        logger.info("Registering commands...")
        CommandRegistrar.registerAll(this)
        logger.info("Registering listeners...")
        ListenerRegistrar.registerAll(this)

        logger.info("Plugin enabled!")
    }

    override fun onDisable() {
        logger.info("Plugin disabled!")
    }
}