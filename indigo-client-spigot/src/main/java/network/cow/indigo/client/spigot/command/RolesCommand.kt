package network.cow.indigo.client.spigot.command

import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.mooapis.indigo.v1.DeleteRoleRequest
import network.cow.mooapis.indigo.v1.GetRoleRequest
import network.cow.mooapis.indigo.v1.InsertRoleRequest
import network.cow.mooapis.indigo.v1.ListRolesRequest
import network.cow.mooapis.indigo.v1.Role
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesCommand(val plugin: IndigoPlugin) : CommandExecutor {

    /*

    /roles add <name>
    /roles delete <name>
    /roles permission list <name>
    /roles permission add <name> <permission>
    /roles permission remove <name> <permission>
    /roles assign <user> <role>
    /roles unassign <user> <role>

     */

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.size == 0) {
            this.sendUsage(sender)
            return true
        }

        when (args[0]) {
            "list" -> list(sender)
            "info" -> {
                if (args.size == 1) {
                    sender.sendMessage("§c/roles info <name>")
                    return true
                }
                info(sender, args[1])
            }
            "add" -> {
                if (args.size == 1) {
                    sender.sendMessage("§c/roles add <name>")
                    return true
                }
                add(sender, args[1])
            }
            "delete" -> {
                if (args.size == 1) {
                    sender.sendMessage("§c/roles delete <name>")
                    return true
                }
                delete(sender, args[1])
            }
            else -> this.sendUsage(sender)
        }
        return true
    }

    private fun sendUsage(sender: CommandSender) {
        sender.sendMessage("§cAvailable commands:")
        sender.sendMessage("§7- /roles list")
        sender.sendMessage("§7- /roles info <name>")
        sender.sendMessage("§7- /roles add <name>")
        sender.sendMessage("§7- /roles delete <name>")
        sender.sendMessage("§7- /roles permission list <name> [page]")
        sender.sendMessage("§7- /roles permission add <name> <permission>")
        sender.sendMessage("§7- /roles permission remove <name> <permission>")
    }

    private fun list(sender: CommandSender) {
        val response = plugin.blockingStub.listRoles(ListRolesRequest.newBuilder().build())

        sender.sendMessage("§aAvailable roles:")
        response.rolesList.forEach {
            sender.sendMessage("§7- §a${it.id} (color: ${it.color})")
        }
    }

    private fun info(sender: CommandSender, name: String) {
        val response = plugin.blockingStub.getRole(GetRoleRequest.newBuilder().setRoleId(name).build())

        val role = response.role
        sender.sendMessage("§aRole info of $name:")
        sender.sendMessage("§7- Priority: §f${role.priority}")
        sender.sendMessage("§7- Color: §f${role.color}")
        sender.sendMessage("§7- Transient: §f${role.transient}")
        sender.sendMessage("§7- Permissions: §f${role.permissionsCount}")
    }

    private fun add(sender: CommandSender, name: String) {
        val response = plugin.blockingStub.insertRole(
            InsertRoleRequest.newBuilder()
                .setRole(
                    Role.newBuilder()
                        .setId(name)
                        .build()
                ).build()
        )

        if (response.insertedRole != null) {
            sender.sendMessage("§aRole $name added.")
        }
    }

    private fun delete(sender: CommandSender, name: String) {
        val response = plugin.blockingStub.deleteRole(
            DeleteRoleRequest.newBuilder()
                .setRoleId(name)
                .build()
        )

        sender.sendMessage("§aRole $name removed.")
    }


}
