package network.cow.indigo.client.spigot.command

import network.cow.cowmands.Arguments
import network.cow.cowmands.Cowmand
import network.cow.indigo.client.spigot.IndigoPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesRefetchCommand(val plugin: IndigoPlugin) : Cowmand() {

    override val label = "refetch"

    override fun execute(sender: CommandSender, args: Arguments) {
        if (args.isEmpty()) {
            // refetch all roles and players
            return
        }

        val targetRaw = args[0]
    }

    override fun tabComplete(sender: CommandSender, args: Arguments): List<String> {
        return plugin.roleCache.getRoles().map { it.name } + Bukkit.getOnlinePlayers().map { "@${it.name}" }
    }


}
