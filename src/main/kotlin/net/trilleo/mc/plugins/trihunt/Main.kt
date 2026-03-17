package net.trilleo.mc.plugins.trihunt

import net.trilleo.mc.plugins.trihunt.registration.CommandRegistrar
import net.trilleo.mc.plugins.trihunt.registration.GUIManager
import net.trilleo.mc.plugins.trihunt.registration.ListenerRegistrar
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        CommandRegistrar.registerAll(this)
        ListenerRegistrar.registerAll(this)
        GUIManager.registerAll(this)
        logger.info("Plugin enabled!")
    }

    override fun onDisable() {
        logger.info("Plugin disabled!")
    }
}