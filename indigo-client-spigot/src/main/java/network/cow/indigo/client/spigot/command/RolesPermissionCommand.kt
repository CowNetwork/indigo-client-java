package network.cow.indigo.client.spigot.command

import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.cowmand.Arguments
import network.cow.indigo.client.spigot.cowmand.Cowmand
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesPermissionCommand(val plugin: IndigoPlugin) : Cowmand() {

    override val label = "permission"
    override val subCommands = listOf(
        RolesPermissionListCommand(plugin), RolesPermissionAddCommand(plugin), RolesPermissionRemoveCommand(plugin)
    )

    override fun execute(sender: CommandSender, args: Arguments) {
        sender.sendMessage("§cAvailable sub commands:")
        sender.sendMessage("§7- /roles permission list (<name>|@<player>)")
        sender.sendMessage("§7- /roles permission add (<name>|@<player>) <permission>")
        sender.sendMessage("§7- /roles permission remove (<name>|@<player>) <permission>")
    }

}
