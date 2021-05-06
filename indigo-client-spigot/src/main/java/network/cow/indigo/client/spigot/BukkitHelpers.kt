package network.cow.indigo.client.spigot

import network.cow.indigo.client.spigot.cache.UserCache
import network.cow.indigo.client.spigot.permission.InjectedPermissibleBase
import network.cow.indigo.client.spigot.permission.getPermissibleBase
import network.cow.indigo.client.spigot.permission.injectPermissibleBase
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Tobias Büser
 */
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
 * Reloads the [InjectedPermissibleBase] of the player.
 */
fun Player.reloadPermissions(cache: UserCache) {
    val user = cache.getUser(this.uniqueId) ?: return

    if (getPermissibleBase(this) !is InjectedPermissibleBase) {
        injectPermissibleBase(this, InjectedPermissibleBase(this, user))
    }
    this.updateCommands()
}
