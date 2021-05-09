package network.cow.indigo.client.spigot.command

import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.cowmands.Arguments
import network.cow.cowmands.Cowmand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesPermissionListCommand(val plugin: IndigoPlugin) : Cowmand() {

    override val label = "list"
    override val usage = "<target>"

    override fun execute(sender: CommandSender, args: Arguments) {
        if (args.isEmpty()) {
            sender.sendMessage("§c/roles permission list <target>")
            return
        }
        val name = args[0]

        val targetsUser = name.startsWith("@")
        var usableName = name
        if (targetsUser) {
            usableName = name.drop(1)
        }

        if (!targetsUser) {
            val role = plugin.cache.getRole(usableName)
            if (role == null) {
                sender.sendMessage("§cRole does not exist.")
                return
            }

            sender.sendMessage("§aPermission list of ${role.name}:")
            role.permissionsList.forEach {
                sender.sendMessage("§7- $it")
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: Arguments): List<String> {
        return plugin.cache.getRoles().map { it.name } + Bukkit.getOnlinePlayers().map { "@${it.name}" }
    }

}
