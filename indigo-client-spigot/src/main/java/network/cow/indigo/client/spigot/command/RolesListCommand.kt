package network.cow.indigo.client.spigot.command

import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.cowmand.Arguments
import network.cow.indigo.client.spigot.cowmand.Cowmand
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesListCommand(val plugin: IndigoPlugin) : Cowmand() {

    override val label = "list"

    override fun execute(sender: CommandSender, args: Arguments) {
        val rolesList = plugin.roleCache.getRoles()
        if (rolesList.isEmpty()) {
            sender.sendMessage("§cThere are no roles available.")
            return
        }

        sender.sendMessage("§aAvailable roles:")
        rolesList.forEach {
            sender.sendMessage("§7- §f${it.name} [${it.id.take(8)}] (color: ${it.color})")
        }
    }

}
