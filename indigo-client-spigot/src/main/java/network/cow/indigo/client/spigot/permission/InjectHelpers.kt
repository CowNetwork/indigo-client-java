package network.cow.indigo.client.spigot.permission

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissibleBase

/**
 * @author Tobias Büser
 */
fun injectPermissibleBase(player: Player, base: PermissibleBase) {
    try {
        val entityClass = Class.forName("org.bukkit.craftbukkit.${getNMSVersion()}.entity.CraftHumanEntity")

        val permField = entityClass.getDeclaredField("perm")
        permField.isAccessible = true
        permField.set(player, base)
    } catch (ex: Exception) {
        // could not inject permission
        ex.printStackTrace()
    }
}

fun getNMSVersion(): String {
    val bukkitPackage = Bukkit.getServer().javaClass.getPackage().name
    return bukkitPackage.substring(bukkitPackage.lastIndexOf(".") + 1)
}
