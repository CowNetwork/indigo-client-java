package network.cow.indigo.client.spigot.command

import io.grpc.Status
import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.handleGrpc
import network.cow.indigo.client.spigot.runAsync
import network.cow.mooapis.indigo.v1.AddRolePermissionsRequest
import network.cow.mooapis.indigo.v1.DeleteRoleRequest
import network.cow.mooapis.indigo.v1.GetRoleRequest
import network.cow.mooapis.indigo.v1.GetRoleResponse
import network.cow.mooapis.indigo.v1.GetUserRolesRequest
import network.cow.mooapis.indigo.v1.GetUserRolesResponse
import network.cow.mooapis.indigo.v1.InsertRoleRequest
import network.cow.mooapis.indigo.v1.InsertRoleResponse
import network.cow.mooapis.indigo.v1.ListRolesRequest
import network.cow.mooapis.indigo.v1.ListRolesResponse
import network.cow.mooapis.indigo.v1.RemoveRolePermissionsRequest
import network.cow.mooapis.indigo.v1.Role
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

/**
 * @author Tobias Büser
 */
class RolesCommand(private val plugin: IndigoPlugin) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            this.sendUsage(sender)
            return true
        }

        runAsync {
            when (args[0]) {
                "list" -> {
                    if (args.size == 1) {
                        list(sender, null)
                    } else {
                        list(sender, args[1])
                    }
                }
                "info" -> {
                    if (args.size == 1) {
                        sender.sendMessage("§c/roles info <name>")
                        return@runAsync
                    }
                    info(sender, args[1])
                }
                "add" -> {
                    if (args.size == 1) {
                        sender.sendMessage("§c/roles add <name>")
                        return@runAsync
                    }
                    add(sender, args[1])
                }
                "delete" -> {
                    if (args.size == 1) {
                        sender.sendMessage("§c/roles delete <name>")
                        return@runAsync
                    }
                    delete(sender, args[1])
                }
                "permission" -> {
                    if (args.size == 1) {
                        sendPermissionUsage(sender)
                        return@runAsync
                    }
                    permission(sender, args.slice(1 until args.size))
                }
                else -> sendUsage(sender)
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): MutableList<String> {
        if (args.size > 2) {
            return mutableListOf()
        }
        if (args.size == 2 && args[0].equals("permission", true)) {
            val subCommands = mutableListOf("list", "add", "remove")

            val current = args[1]
            if (current.isEmpty()) {
                return subCommands
            }
            return subCommands.filter { it.startsWith(current) }.toMutableList()
        }

        val subCommands = mutableListOf("list", "info", "add", "delete", "permission")

        val current = args[0]
        if (current.isEmpty()) {
            return subCommands
        }
        return subCommands.filter { it.startsWith(current) }.toMutableList()
    }

    private fun sendUsage(sender: CommandSender) {
        sender.sendMessage("§cAvailable commands:")
        sender.sendMessage("§7- /roles list [player]")
        sender.sendMessage("§7- /roles info <name>")
        sender.sendMessage("§7- /roles add <name>")
        sender.sendMessage("§7- /roles delete <name>")
        sender.sendMessage("§7- /roles permission list <name>")
        sender.sendMessage("§7- /roles permission add <name> <permission>")
        sender.sendMessage("§7- /roles permission remove <name> <permission>")
        sender.sendMessage("§7- /roles assign <player> <role>")
        sender.sendMessage("§7- /roles unassign <player> <role>")
    }

    private fun sendPermissionUsage(sender: CommandSender) {
        sender.sendMessage("§cAvailable sub commands:")
        sender.sendMessage("§7- /roles permission list <name>")
        sender.sendMessage("§7- /roles permission add <name> <permission>")
        sender.sendMessage("§7- /roles permission remove <name> <permission>")
    }

    private fun list(sender: CommandSender, player: String?) {
        if (player != null) {
            var response: GetUserRolesResponse? = null
            val status = handleGrpc {
                response = plugin.blockingStub.getUserRoles(
                    GetUserRolesRequest.newBuilder().setUserAccountId(player).build()
                )
            }
            if (!status.isOk()) {
                status.handle(Status.Code.NOT_FOUND) {
                    sender.sendMessage("§cUser does not have any roles.")
                }.handleDefault(sender)
                return
            }

            sender.sendMessage("§aUser has roles:")
            response!!.rolesList.forEach {
                sender.sendMessage("§7- §f${it.id} (color: ${it.color})")
            }
            return
        }

        var response: ListRolesResponse? = null
        val status = handleGrpc {
            response = plugin.blockingStub.listRoles(ListRolesRequest.newBuilder().build())
        }
        if (!status.isOk()) {
            status.handleDefault(sender)
            return
        }

        sender.sendMessage("§aAvailable roles:")
        response!!.rolesList.forEach {
            sender.sendMessage("§7- §f${it.id} (color: ${it.color})")
        }
    }

    private fun info(sender: CommandSender, name: String) {
        var response: GetRoleResponse? = null
        val status = handleGrpc {
            response = plugin.blockingStub.getRole(GetRoleRequest.newBuilder().setRoleId(name).build())
        }
        if (!status.isOk()) {
            status.handle(Status.Code.NOT_FOUND) {
                sender.sendMessage("§cRole does not exist.")
            }.handleDefault(sender)
            return
        }

        val role = response!!.role
        sender.sendMessage("§aRole info of $name:")
        sender.sendMessage("§7- Priority: §f${role.priority}")
        sender.sendMessage("§7- Color: §f${role.color}")
        sender.sendMessage("§7- Transient: §f${role.transient}")
        sender.sendMessage("§7- Permissions: §f${role.permissionsCount}")
    }

    private fun add(sender: CommandSender, name: String) {
        var response: InsertRoleResponse? = null
        val status = handleGrpc {
            response = plugin.blockingStub.insertRole(
                InsertRoleRequest.newBuilder().setRole(Role.newBuilder().setId(name).build()).build()
            )
        }
        if (!status.isOk()) {
            status.handle(Status.Code.ALREADY_EXISTS) {
                sender.sendMessage("§cRole already exists.")
            }.handleDefault(sender)
            return
        }

        if (response!!.insertedRole != null) {
            sender.sendMessage("§aRole $name added.")
        }
    }

    private fun delete(sender: CommandSender, name: String) {
        val status = handleGrpc {
            plugin.blockingStub.deleteRole(
                DeleteRoleRequest.newBuilder()
                    .setRoleId(name)
                    .build()
            )
        }
        if (!status.isOk()) {
            status.handle(Status.Code.NOT_FOUND) {
                sender.sendMessage("§cRole does not exist.")
            }.handleDefault(sender)
            return
        }

        sender.sendMessage("§aRole $name removed.")
    }

    private fun permission(sender: CommandSender, args: List<String>) {
        when (args[0]) {
            "list" -> {
                if (args.size == 1) {
                    sender.sendMessage("§c/roles permission list <name>")
                    return
                }
                this.permissionList(sender, args[1])
            }
            "add" -> {
                if (args.size <= 2) {
                    sender.sendMessage("§c/roles permission add <name> <permission>")
                    return
                }
                this.permissionAdd(sender, args[1], args[2])
            }
            "remove" -> {
                if (args.size <= 2) {
                    sender.sendMessage("§c/roles permission remove <name> <permission>")
                    return
                }
                this.permissionRemove(sender, args[1], args[2])
            }
            else -> {
                sendPermissionUsage(sender)
            }
        }
    }

    private fun permissionList(sender: CommandSender, name: String) {
        var response: GetRoleResponse? = null
        val status = handleGrpc {
            response = plugin.blockingStub.getRole(GetRoleRequest.newBuilder().setRoleId(name).build())
        }
        if (!status.isOk()) {
            status.handle(Status.Code.NOT_FOUND) {
                sender.sendMessage("§cRole does not exist.")
            }.handleDefault(sender)
            return
        }

        val role = response!!.role
        sender.sendMessage("§aPermission list of ${role.id}:")
        role.permissionsList.forEach {
            sender.sendMessage("§7- $it")
        }
    }

    private fun permissionAdd(sender: CommandSender, name: String, permission: String) {
        val status = handleGrpc {
            plugin.blockingStub.addRolePermissions(
                AddRolePermissionsRequest.newBuilder()
                    .setRoleId(name)
                    .addPermissions(permission)
                    .build()
            )
        }
        if (!status.isOk()) {
            status.handle(Status.Code.NOT_FOUND) {
                sender.sendMessage("§cRole does not exist.")
            }.handleDefault(sender)
            return
        }

        sender.sendMessage("§aPermission added.")
    }

    private fun permissionRemove(sender: CommandSender, name: String, permission: String) {
        val status = handleGrpc {
            plugin.blockingStub.removeRolePermissions(
                RemoveRolePermissionsRequest.newBuilder()
                    .setRoleId(name)
                    .addPermissions(permission)
                    .build()
            )
        }
        if (!status.isOk()) {
            status.handle(Status.Code.NOT_FOUND) {
                sender.sendMessage("§cRole does not exist.")
            }.handleDefault(sender)
            return
        }

        sender.sendMessage("§aPermission removed.")
    }


}
