package network.cow.indigo.client.spigot.command

import network.cow.cowmands.Arguments
import network.cow.cowmands.Cowmand
import network.cow.indigo.client.spigot.IndigoPlugin
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesCommand(val plugin: IndigoPlugin) : Cowmand() {

    override val label = "roles"
    override val subCommands = listOf(
        RolesCreateCommand(plugin), RolesInfoCommand(plugin),
        RolesDeleteCommand(plugin), RolesListCommand(plugin),
        RolesGetCommand(plugin), RolesAddCommand(plugin),
        RolesRemoveCommand(plugin), RolesPermissionCommand(plugin),
        RolesRefetchCommand(plugin)
    )
    override val permission = "cow.indigo.command.roles"

    override fun execute(sender: CommandSender, args: Arguments) {
        sender.sendMessage("§cAvailable commands:")
        sender.sendMessage("§7- /roles list")
        sender.sendMessage("§7- /roles info <name>")
        sender.sendMessage("§7- /roles create <name>")
        sender.sendMessage("§7- /roles delete <name>")
        sender.sendMessage("§7- /roles permission list <name>")
        sender.sendMessage("§7- /roles permission add <name> <permission>")
        sender.sendMessage("§7- /roles permission remove <name> <permission>")
        sender.sendMessage("§7- /roles get <player>")
        sender.sendMessage("§7- /roles add <player> <role>")
        sender.sendMessage("§7- /roles remove <player> <role>")
        sender.sendMessage("§7- /roles refetch [<target>]")
    }

}
