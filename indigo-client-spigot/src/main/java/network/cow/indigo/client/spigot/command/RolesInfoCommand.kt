package network.cow.indigo.client.spigot.command

import network.cow.cowmands.Arguments
import network.cow.cowmands.Cowmand
import network.cow.indigo.client.spigot.IndigoPlugin
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesInfoCommand(private val plugin: IndigoPlugin) : Cowmand() {

    override val label = "info"
    override val usage = "<name>"

    override fun execute(sender: CommandSender, args: Arguments) {
        if (args.isEmpty()) {
            sender.sendMessage("§c/roles info <name>")
            return
        }

        val name = args[0]
        val role = plugin.cache.getRole(name)
        if (role == null) {
            sender.sendMessage("§cRole does not exist.")
            return
        }

        sender.sendMessage("§aRole info of ${role.name}:")
        sender.sendMessage("§7- Priority: §f${role.priority}")
        sender.sendMessage("§7- Color: §f${role.color}")
        sender.sendMessage("§7- Transient: §f${role.transient}")
        sender.sendMessage("§7- Permissions: §f${role.permissionsCount}")
    }

    override fun tabComplete(sender: CommandSender, args: Arguments): List<String> {
        return plugin.cache.getRoles().map { it.name }
    }
}
