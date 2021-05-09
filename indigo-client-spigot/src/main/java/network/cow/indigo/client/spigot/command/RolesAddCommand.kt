package network.cow.indigo.client.spigot.command

import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.cowmands.Arguments
import network.cow.cowmands.Cowmand
import network.cow.indigo.client.spigot.handleGrpc
import network.cow.mooapis.indigo.v1.AddUserRolesRequest
import network.cow.mooapis.indigo.v1.RoleIdentifier
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesAddCommand(private val plugin: IndigoPlugin) : Cowmand() {

    override val label = "add"
    override val usage = "<player> <role>"

    override fun execute(sender: CommandSender, args: Arguments) {
        if (args.size <= 1) {
            sender.sendMessage("§c/roles add <player> <role>")
            return
        }
        val playerName = args[0]
        val roleName = args[0]

        val player = Bukkit.getPlayer(playerName)
        if (player == null) {
            sender.sendMessage("§cThis player is not online!")
            return
        }

        val role = plugin.cache.getRole(roleName)
        if (role == null) {
            sender.sendMessage("§cRole does not exist.")
            return
        }

        val indigoUser = plugin.cache.getUser(player.uniqueId)
        if (indigoUser != null && indigoUser.hasRole(roleName)) {
            sender.sendMessage("§cThis player already has this role.")
            return
        }

        val status = handleGrpc {
            plugin.stub.addUserRoles(
                AddUserRolesRequest.newBuilder()
                    .setUserAccountId(player.uniqueId.toString())
                    .addRoleIds(RoleIdentifier.newBuilder().setUuid(role.id).build())
                    .build()
            )
        }
        if (!status.isOk()) {
            status.handleCommandDefault(sender)
            return
        }
        sender.sendMessage("§aRole added.")
    }

    override fun tabComplete(sender: CommandSender, args: Arguments): List<String> {
        return if (args.isEmpty()) {
            Bukkit.getOnlinePlayers().map { it.name }
        } else {
            plugin.cache.getRoles().map { it.name }
        }
    }
}
