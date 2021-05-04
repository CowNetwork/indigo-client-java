package network.cow.indigo.client.spigot.command

import network.cow.indigo.client.spigot.IndigoPlugin
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesCommand(val plugin: IndigoPlugin) : SupCommand() {

    override val label = "roles"
    override val aliases = listOf("cock", "assbitch")
    override val subCommands = listOf(
        RolesCreateCommand(plugin), RolesInfoCommand(plugin),
        RolesDeleteCommand(plugin), RolesListCommand(plugin)
    )
    override val permission = "cow.indigo.command.roles"

    override fun execute(sender: CommandSender, args: Arguments) {
        sender.sendMessage("RolesCommand#execute()")
    }

    override fun tabComplete(sender: CommandSender, args: Arguments): List<String> {
        return subCommands.map { it.label }
    }

}
