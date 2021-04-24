package network.cow.indigo.client.spigot

import org.bukkit.Bukkit
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
