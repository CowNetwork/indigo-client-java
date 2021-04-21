package network.cow.indigo.client.spigot.command

import io.grpc.Status
import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.createRole
import network.cow.indigo.client.spigot.createRoleIdentifierOf
import network.cow.indigo.client.spigot.handleGrpc
import network.cow.indigo.client.spigot.runAsync
import network.cow.mooapis.indigo.v1.AddRolePermissionsRequest
import network.cow.mooapis.indigo.v1.DeleteRoleRequest
import network.cow.mooapis.indigo.v1.GetUserRolesRequest
import network.cow.mooapis.indigo.v1.GetUserRolesResponse
import network.cow.mooapis.indigo.v1.InsertRoleRequest
import network.cow.mooapis.indigo.v1.InsertRoleResponse
import network.cow.mooapis.indigo.v1.RemoveRolePermissionsRequest
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.util.StringUtil


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
        val completions = mutableListOf<String>()

        if (args.isEmpty()) {
            return mutableListOf()
        }
        val subCommand = args[0]

        if (args.size == 1) {
            val subCommands = mutableListOf("list", "info", "add", "delete", "permission")

            StringUtil.copyPartialMatches(subCommand, subCommands, completions)
        } else if (args.size == 2) {
            when (subCommand) {
                "permission" -> {
                    val subCommands = mutableListOf("list", "add", "remove")

                    StringUtil.copyPartialMatches(args[1], subCommands, completions)
                }
                "info", "delete" -> {
                    val roles = plugin.roleCache.getRoles().map { it.name }

                    StringUtil.copyPartialMatches(args[1], roles, completions)
                }
            }
        } else if (args.size == 3 && subCommand == "permission") {
            val subCommands = mutableListOf("list", "add", "remove")
            if (!subCommands.contains(args[1])) {
                return completions
            }

            val roles = plugin.roleCache.getRoles().map { it.name }

            StringUtil.copyPartialMatches(args[2], roles, completions)
        }

        completions.sort()
        return completions
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
                }.handleCommandDefault(sender)
                return
            }

            sender.sendMessage("§aUser has roles:")
            response!!.rolesList.forEach {
                sender.sendMessage("§7- §f${it.id} (color: ${it.color})")
            }
            return
        }

        val rolesList = plugin.roleCache.getRoles()
        if (rolesList.isEmpty()) {
            sender.sendMessage("§cThere are no roles available.")
            return
        }

        sender.sendMessage("§aAvailable roles:")
        rolesList.forEach {
            sender.sendMessage("§7- §f${it.id} (color: ${it.color})")
        }
    }

    private fun info(sender: CommandSender, name: String) {
        val role = plugin.roleCache.getRole(name)
        if (role == null) {
            sender.sendMessage("§cRole does not exist.")
            return
        }

        sender.sendMessage("§aRole info of $name:")
        sender.sendMessage("§7- Priority: §f${role.priority}")
        sender.sendMessage("§7- Color: §f${role.color}")
        sender.sendMessage("§7- Transient: §f${role.transient}")
        sender.sendMessage("§7- Permissions: §f${role.permissionsCount}")
    }

    private fun add(sender: CommandSender, name: String) {
        val role = plugin.roleCache.getRole(name)
        if (role != null) {
            sender.sendMessage("§cRole already exists.")
            return
        }

        var response: InsertRoleResponse? = null
        val status = handleGrpc {
            response = plugin.blockingStub.insertRole(
                InsertRoleRequest.newBuilder().setRole(createRole(name)).build()
            )
        }
        if (!status.isOk()) {
            status.handle(Status.Code.ALREADY_EXISTS) {
                sender.sendMessage("§4Service already knows this role.")
            }.handleCommandDefault(sender)
            return
        }

        if (response!!.insertedRole != null) {
            sender.sendMessage("§aRole $name added.")
        }
    }

    private fun delete(sender: CommandSender, name: String) {
        val role = plugin.roleCache.getRole(name)
        if (role == null) {
            sender.sendMessage("§cRole does not exist.")
            return
        }

        val status = handleGrpc {
            plugin.blockingStub.deleteRole(
                DeleteRoleRequest.newBuilder()
                    .setRoleId(createRoleIdentifierOf(name))
                    .build()
            )
        }
        if (!status.isOk()) {
            status.handle(Status.Code.NOT_FOUND) {
                sender.sendMessage("§4Service could not find role.")
            }.handleCommandDefault(sender)
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
        val role = plugin.roleCache.getRole(name)
        if (role == null) {
            sender.sendMessage("§cRole does not exist.")
            return
        }

        sender.sendMessage("§aPermission list of ${role.id}:")
        role.permissionsList.forEach {
            sender.sendMessage("§7- $it")
        }
    }

    private fun permissionAdd(sender: CommandSender, name: String, permission: String) {
        val role = plugin.roleCache.getRole(name)
        if (role == null) {
            sender.sendMessage("§cRole does not exist.")
            return
        }
        if (role.permissionsList.contains(permission)) {
            sender.sendMessage("§cRole already has this permission.")
            return
        }

        val status = handleGrpc {
            plugin.blockingStub.addRolePermissions(
                AddRolePermissionsRequest.newBuilder()
                    .setRoleId(createRoleIdentifierOf(name))
                    .addPermissions(permission)
                    .build()
            )
        }
        if (!status.isOk()) {
            status.handle(Status.Code.NOT_FOUND) {
                sender.sendMessage("§4Service could not find role.")
            }.handleCommandDefault(sender)
            return
        }

        sender.sendMessage("§aPermission added.")
    }

    private fun permissionRemove(sender: CommandSender, name: String, permission: String) {
        val role = plugin.roleCache.getRole(name)
        if (role == null) {
            sender.sendMessage("§cRole does not exist.")
            return
        }
        if (!role.permissionsList.contains(permission)) {
            sender.sendMessage("§cRole does not have this permission.")
            return
        }

        val status = handleGrpc {
            plugin.blockingStub.removeRolePermissions(
                RemoveRolePermissionsRequest.newBuilder()
                    .setRoleId(createRoleIdentifierOf(name))
                    .addPermissions(permission)
                    .build()
            )
        }
        if (!status.isOk()) {
            status.handle(Status.Code.NOT_FOUND) {
                sender.sendMessage("§4Service could not find role.")
            }.handleCommandDefault(sender)
            return
        }

        sender.sendMessage("§aPermission removed.")
    }


}
