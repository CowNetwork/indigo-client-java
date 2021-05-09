package network.cow.indigo.client.spigot.command

import io.grpc.Status
import network.cow.cowmands.Arguments
import network.cow.cowmands.Cowmand
import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.createRoleIdentifierOf
import network.cow.indigo.client.spigot.handleGrpcBetter
import network.cow.mooapis.indigo.v1.GetRoleRequest
import network.cow.mooapis.indigo.v1.ListRolesRequest
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesRefetchCommand(val plugin: IndigoPlugin) : Cowmand() {

    override val label = "refetch"

    override fun execute(sender: CommandSender, args: Arguments) {
        if (args.isEmpty()) {
            // refetch all roles
            sender.sendMessage("§7Reloading roles ...")

            val (status, response) = handleGrpcBetter {
                plugin.stub.listRoles(ListRolesRequest.newBuilder().build())
            }
            if (!status.isOk()) {
                status.handleCommandDefault(sender)
                return
            }

            plugin.cache.replaceRoles(response!!.rolesList)
            sender.sendMessage("§aReloaded roles.")
            return
        }

        val name = args[0]
        val role = plugin.cache.getRole(name)
        if (role == null) {
            sender.sendMessage("§cRole does not exist.")
            return
        }

        val (status, response) = handleGrpcBetter {
            plugin.stub.getRole(GetRoleRequest.newBuilder().setRoleId(createRoleIdentifierOf(name)).build())
        }
        if (!status.isOk()) {
            status.handle(Status.Code.NOT_FOUND) {
                plugin.cache.deleteRoles(role)
                sender.sendMessage("§c$name got deleted in database. Deleted it in cache as well.")
            }.handleCommandDefault(sender)
            return
        }

        plugin.cache.updateRoles(response!!.role)
        sender.sendMessage("§aRefetched role $name.")
    }

    override fun tabComplete(sender: CommandSender, args: Arguments): List<String> {
        return plugin.cache.getRoles().map { it.name } + Bukkit.getOnlinePlayers().map { "@${it.name}" }
    }


}
