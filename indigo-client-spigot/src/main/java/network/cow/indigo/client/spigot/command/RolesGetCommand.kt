package network.cow.indigo.client.spigot.command

import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.cowmands.Arguments
import network.cow.cowmands.Cowmand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesGetCommand(private val plugin: IndigoPlugin) : Cowmand() {

    override val label = "get"
    override val usage = "<player>"

    override fun execute(sender: CommandSender, args: Arguments) {
        if (args.isEmpty()) {
            sender.sendMessage("§c/roles get <player>")
            return
        }
        val playerName = args[0]

        val player = Bukkit.getPlayer(playerName)
        if (player == null) {
            sender.sendMessage("§cThis player is not online!")
            return
        }

        val indigoUser = plugin.userCache.getUser(player.uniqueId)
        if (indigoUser == null) {
            sender.sendMessage("§cThis player does not have any roles.")
            return
        }

        val roles = indigoUser.roles
        if (roles.isEmpty()) {
            sender.sendMessage("§cThis player does not have any roles.")
            return
        }

        sender.sendMessage("§aUser has roles:")
        roles.forEach {
            sender.sendMessage("§7- §f${it.name} (priority: ${it.priority}, transient: ${it.transient})")
        }
    }

    override fun tabComplete(sender: CommandSender, args: Arguments): List<String> {
        return Bukkit.getOnlinePlayers().map { it.name }
    }
}
