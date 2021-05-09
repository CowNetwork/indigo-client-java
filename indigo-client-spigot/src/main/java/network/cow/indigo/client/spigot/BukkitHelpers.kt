package network.cow.indigo.client.spigot

import network.cow.indigo.client.spigot.api.IndigoUser
import network.cow.indigo.client.spigot.api.event.PermissionUpdateEvent
import network.cow.indigo.client.spigot.permission.InjectedPermissibleBase
import network.cow.indigo.client.spigot.permission.getPermissibleBase
import network.cow.indigo.client.spigot.permission.injectPermissibleBase
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin

fun callEvent(plugin: JavaPlugin, event: Event) {
    if (!Bukkit.isPrimaryThread()) {
        Bukkit.getScheduler().runTask(plugin, object : Runnable {
            override fun run() {
                Bukkit.getPluginManager().callEvent(event)
            }
        })
        return
    }
    Bukkit.getPluginManager().callEvent(event)
}

/**
 * Reloads the [InjectedPermissibleBase] of the player and calls
 * an [PermissionUpdateEvent].
 */
fun Player.reloadPermissions() {
    this.reloadPermissionsSilently()

    val plugin = JavaPlugin.getPlugin(IndigoPlugin::class.java)
    callEvent(plugin, PermissionUpdateEvent(this))
}

fun Player.reloadPermissionsSilently() {
    val plugin = JavaPlugin.getPlugin(IndigoPlugin::class.java)
    val user = plugin.cache.getUser(this.uniqueId) ?: return

    if (getPermissibleBase(this) !is InjectedPermissibleBase) {
        injectPermissibleBase(this, InjectedPermissibleBase(this, user))
    }
    this.updateCommands()
}

fun IndigoUser.reloadPermissions() {
    val player = Bukkit.getPlayer(this.uuid) ?: return
    player.reloadPermissions()
}
