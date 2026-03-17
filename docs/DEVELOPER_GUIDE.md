# TriHunt - Developer Guide

This guide explains how to create **commands**, **listeners**, and **GUIs** using TriHunt's registration system. All three follow the same pattern: extend a base class (or implement an interface), place the file in the correct package, and the plugin handles the rest automatically at startup.

## How Auto-Registration Works

TriHunt uses a `PackageScanner` to discover classes at runtime. When the plugin starts, it scans specific packages for concrete (non-abstract) classes and registers them automatically. You never need to edit `plugin.yml` or manually wire anything up.

| System   | Base Class / Interface | Package                                          |
|:---------|:-----------------------|:-------------------------------------------------|
| Commands | `PluginCommand`        | `net.trilleo.mc.plugins.trihunt.commands`        |
| Listeners| `Listener`             | `net.trilleo.mc.plugins.trihunt.listeners`       |
| GUIs     | `PluginGUI`            | `net.trilleo.mc.plugins.trihunt.guis`            |

Subpackages are also scanned, so you can freely organize classes into folders like `commands/game/`, `listeners/player/`, or `guis/menus/`.

## Constructor Requirements

Every command, listener, and GUI class must have one of the following constructors:

| Constructor                          | When to Use                                   |
|:-------------------------------------|:----------------------------------------------|
| No-arg constructor                   | When you don't need a reference to the plugin |
| Constructor accepting a `JavaPlugin` | When you need to access the plugin instance   |

The plugin instance is injected automatically when a `JavaPlugin` constructor is available.

---

## Commands

To create a command, extend `PluginCommand` and place the class anywhere inside the `commands` package or a subpackage.

By default every command is registered as a **sub-command** of `/trihunt` (alias `/th`). For example, a command with `name = "reload"` becomes `/trihunt reload`. Set `isMainCommand = true` to register the command as a standalone top-level command instead.

When a player types `/trihunt` in-game, tab-completion automatically lists all available sub-commands.

### Categories

Commands are automatically categorised based on their **subpackage** (folder) inside the `commands` package. The category is used by the built-in `/trihunt help` command to group commands for display.

| Command Location                       | Category    |
|:---------------------------------------|:------------|
| `commands/PingCommand.kt`              | General     |
| `commands/game/StartCommand.kt`        | Game        |
| `commands/admin/BanCommand.kt`         | Admin       |

### Help Command

The plugin ships with a built-in `/trihunt help` command. It lists every registered command grouped by category, sorted alphabetically within each group, and formatted with colours for readability. Every command should provide a meaningful `description` so the help output is informative.

### PluginCommand Properties

| Property        | Type           | Default        | Description                                                                 |
|:----------------|:---------------|:---------------|:----------------------------------------------------------------------------|
| `name`          | `String`       | *(required)*   | The command name (e.g. `"reload"` for `/trihunt reload`)                    |
| `description`   | `String`       | `""`           | A brief description shown in `/trihunt help` — always provide one           |
| `usage`         | `String`       | `"/<command>"` | Usage hint shown when the command fails                                     |
| `aliases`       | `List<String>` | `emptyList()`  | Alternative names for the command (applicable to main commands only)         |
| `permission`    | `String?`      | `null`         | Permission node required to use the command                                 |
| `isMainCommand` | `Boolean`      | `false`        | When `true`, the command is registered as a standalone top-level command     |

### Methods to Override

| Method        | Required | Description                                       |
|:--------------|:---------|:--------------------------------------------------|
| `execute`     | Yes      | Called when a player or console runs the command   |
| `tabComplete` | No       | Called when tab-completion is requested             |

### Example (Sub-Command)

This command is registered as `/trihunt ping` (the default behavior):

```kotlin
package net.trilleo.mc.plugins.trihunt.commands

import net.trilleo.mc.plugins.trihunt.registration.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PingCommand : PluginCommand(
    name = "ping",
    description = "Check your latency",
    usage = "/trihunt ping",
    permission = "trihunt.ping"
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }
        sender.sendMessage("Pong! Your ping is ${sender.ping}ms.")
        return true
    }
}
```

### Example with Tab Completion (Sub-Command)

This command is registered as `/trihunt team`:

```kotlin
package net.trilleo.mc.plugins.trihunt.commands.game

import net.trilleo.mc.plugins.trihunt.registration.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TeamCommand : PluginCommand(
    name = "team",
    description = "Join a team",
    usage = "/trihunt team <hunters|runners>",
    permission = "trihunt.team"
) {
    private val teams = listOf("hunters", "runners")

    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0] !in teams) {
            sender.sendMessage("Usage: /trihunt team <hunters|runners>")
            return false
        }
        sender.sendMessage("You joined the ${args[0]} team!")
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 1) {
            return teams.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }
}
```

### Example with Plugin Instance (Sub-Command)

This command is registered as `/trihunt reload`:

```kotlin
package net.trilleo.mc.plugins.trihunt.commands

import net.trilleo.mc.plugins.trihunt.registration.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class ReloadCommand(private val plugin: JavaPlugin) : PluginCommand(
    name = "reload",
    description = "Reload the plugin configuration",
    permission = "trihunt.reload"
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        plugin.reloadConfig()
        sender.sendMessage("Configuration reloaded!")
        return true
    }
}
```

### Example (Main Command)

Set `isMainCommand = true` to register a standalone top-level command.
This command is registered as `/globaltool`:

```kotlin
package net.trilleo.mc.plugins.trihunt.commands

import net.trilleo.mc.plugins.trihunt.registration.PluginCommand
import org.bukkit.command.CommandSender

class GlobalToolCommand : PluginCommand(
    name = "globaltool",
    description = "A standalone top-level command",
    usage = "/globaltool",
    isMainCommand = true
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        sender.sendMessage("Hello from /globaltool!")
        return true
    }
}
```

---

## Listeners

To create a listener, implement Bukkit's `Listener` interface and place the class anywhere inside the `listeners` package or a subpackage.

### Methods

Annotate each event handler method with `@EventHandler`. The method must accept a single Bukkit event parameter.

### Example

```kotlin
package net.trilleo.mc.plugins.trihunt.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(
            net.kyori.adventure.text.Component.text("Welcome, ${event.player.name}!")
        )
    }
}
```

### Example with Subpackage and Plugin Instance

```kotlin
package net.trilleo.mc.plugins.trihunt.listeners.player

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin

class DeathListener(private val plugin: JavaPlugin) : Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        plugin.logger.info("${event.player.name} has been eliminated!")
    }
}
```

---

## GUIs

To create a GUI (chest-based inventory menu), extend `PluginGUI` and place the class anywhere inside the `guis` package or a subpackage.

### PluginGUI Properties

| Property | Type     | Default      | Description                             |
|:---------|:---------|:-------------|:----------------------------------------|
| `id`     | `String` | *(required)* | Unique identifier used to open the GUI  |
| `title`  | `String` | *(required)* | Title displayed at the top of the chest |
| `rows`   | `Int`    | `3`          | Number of rows (1–6, each row = 9 slots)|

### Methods to Override

| Method    | Required | Description                                          |
|:----------|:---------|:-----------------------------------------------------|
| `setup`   | Yes      | Populate the inventory with items before it opens    |
| `onClick` | No       | Handle click events (clicks are cancelled by default)|
| `onClose` | No       | Handle cleanup when the GUI is closed                |

### Opening a GUI

Use `GUIManager.open(player, id)` to open a registered GUI for a player:

```kotlin
import net.trilleo.mc.plugins.trihunt.registration.GUIManager

// Returns true if the GUI was found and opened, false otherwise
GUIManager.open(player, "settings")
```

### Example

```kotlin
package net.trilleo.mc.plugins.trihunt.guis

import net.trilleo.mc.plugins.trihunt.registration.PluginGUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class SettingsGUI : PluginGUI(
    id = "settings",
    title = "Settings",
    rows = 3
) {
    override fun setup(player: Player, inventory: Inventory) {
        val compass = ItemStack(Material.COMPASS)
        val meta = compass.itemMeta
        meta.displayName(net.kyori.adventure.text.Component.text("Tracker"))
        compass.itemMeta = meta
        inventory.setItem(13, compass)
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        if (event.slot == 13) {
            player.sendMessage("Tracker selected!")
        }
    }
}
```

### Opening a GUI from a Command

A common pattern is opening a GUI when a player runs a command. This command is registered as `/trihunt settings`:

```kotlin
package net.trilleo.mc.plugins.trihunt.commands

import net.trilleo.mc.plugins.trihunt.registration.GUIManager
import net.trilleo.mc.plugins.trihunt.registration.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SettingsCommand : PluginCommand(
    name = "settings",
    description = "Open the settings menu",
    permission = "trihunt.settings"
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }
        GUIManager.open(sender, "settings")
        return true
    }
}
```
