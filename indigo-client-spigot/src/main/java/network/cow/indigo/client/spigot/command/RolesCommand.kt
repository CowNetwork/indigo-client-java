package network.cow.indigo.client.spigot.command

import io.grpc.Status
import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.mooapis.indigo.v1.AddRolePermissionRequest
import network.cow.mooapis.indigo.v1.DeleteRoleRequest
import network.cow.mooapis.indigo.v1.GetRoleRequest
import network.cow.mooapis.indigo.v1.GetUserRolesRequest
import network.cow.mooapis.indigo.v1.InsertRoleRequest
import network.cow.mooapis.indigo.v1.ListRolesRequest
import network.cow.mooapis.indigo.v1.RemoveRolePermissionRequest
import network.cow.mooapis.indigo.v1.Role
import org.bukkit.Bukkit
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, object : Runnable {
            override fun run() {
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
                            return
                        }
                        info(sender, args[1])
                    }
                    "add" -> {
                        if (args.size == 1) {
                            sender.sendMessage("§c/roles add <name>")
                            return
                        }
                        add(sender, args[1])
                    }
                    "delete" -> {
                        if (args.size == 1) {
                            sender.sendMessage("§c/roles delete <name>")
                            return
                        }
                        delete(sender, args[1])
                    }
                    "permission" -> {
                        if (args.size == 1) {
                            sender.sendMessage("§cAvailable sub commands:")
                            sender.sendMessage("§7- /roles permission list <name>")
                            sender.sendMessage("§7- /roles permission add <name> <permission>")
                            sender.sendMessage("§7- /roles permission remove <name> <permission>")
                            return
                        }
                        permission(sender, args.slice(1..args.size))
                    }
                    else -> sendUsage(sender)
                }
            }
        })
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

    private fun list(sender: CommandSender, player: String?) {
        if (player != null) {
            try {
                val response = plugin.blockingStub.getUserRoles(GetUserRolesRequest.newBuilder().setUserAccountId(player).build())

                sender.sendMessage("§aUser has roles:")
                response.rolesList.forEach {
                    sender.sendMessage("§7- §f${it.id} (color: ${it.color})")
                }
            } catch (ex: Exception) {
                val status = Status.fromThrowable(ex)

                when (status.code) {
                    Status.Code.NOT_FOUND -> {
                        sender.sendMessage("§cUser does not have any roles.")
                    }
                    Status.Code.UNAVAILABLE -> {
                        sender.sendMessage("§cThe service is currently offline. Please try again later.")
                    }
                    else -> {
                        sender.sendMessage("§4There have been an error during the request. Please look into the log.")
                        ex.printStackTrace()
                    }
                }
            }
            return
        }

        try {
            val response = plugin.blockingStub.listRoles(ListRolesRequest.newBuilder().build())

            sender.sendMessage("§aAvailable roles:")
            response.rolesList.forEach {
                sender.sendMessage("§7- §f${it.id} (color: ${it.color})")
            }
        } catch (ex: Exception) {
            val status = Status.fromThrowable(ex)

            when (status.code) {
                Status.Code.UNAVAILABLE -> {
                    sender.sendMessage("§cThe service is currently offline. Please try again later.")
                }
                else -> {
                    sender.sendMessage("§4There have been an error during the request. Please look into the log.")
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun info(sender: CommandSender, name: String) {
        try {
            val response = plugin.blockingStub.getRole(GetRoleRequest.newBuilder().setRoleId(name).build())

            val role = response.role
            sender.sendMessage("§aRole info of $name:")
            sender.sendMessage("§7- Priority: §f${role.priority}")
            sender.sendMessage("§7- Color: §f${role.color}")
            sender.sendMessage("§7- Transient: §f${role.transient}")
            sender.sendMessage("§7- Permissions: §f${role.permissionsCount}")
        } catch (ex: Exception) {
            val status = Status.fromThrowable(ex)

            when (status.code) {
                Status.Code.NOT_FOUND -> {
                    sender.sendMessage("§cRole does not exist.")
                }
                Status.Code.UNAVAILABLE -> {
                    sender.sendMessage("§cThe service is currently offline. Please try again later.")
                }
                else -> {
                    sender.sendMessage("§4There have been an error during the request. Please look into the log.")
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun add(sender: CommandSender, name: String) {
        try {
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
        } catch (ex: Exception) {
            val status = Status.fromThrowable(ex)

            when (status.code) {
                Status.Code.ALREADY_EXISTS -> {
                    sender.sendMessage("§cRole already exists.")
                }
                Status.Code.UNAVAILABLE -> {
                    sender.sendMessage("§cThe service is currently offline. Please try again later.")
                }
                else -> {
                    sender.sendMessage("§4There have been an error during the request. Please look into the log.")
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun delete(sender: CommandSender, name: String) {
        try {
            plugin.blockingStub.deleteRole(
                DeleteRoleRequest.newBuilder()
                    .setRoleId(name)
                    .build()
            )

            sender.sendMessage("§aRole $name removed.")
        } catch (ex: Exception) {
            val status = Status.fromThrowable(ex)

            when (status.code) {
                Status.Code.NOT_FOUND -> {
                    sender.sendMessage("§cRole does not exist.")
                }
                Status.Code.UNAVAILABLE -> {
                    sender.sendMessage("§cThe service is currently offline. Please try again later.")
                }
                else -> {
                    sender.sendMessage("§4There have been an error during the request. Please look into the log.")
                    ex.printStackTrace()
                }
            }
        }
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
        }
    }

    private fun permissionList(sender: CommandSender, name: String) {
        try {
            val response = plugin.blockingStub.getRole(GetRoleRequest.newBuilder().setRoleId(name).build())

            val role = response.role
            sender.sendMessage("§aPermission list of $role:")
            role.permissionsList.forEach {
                sender.sendMessage("§7- $it")
            }
        } catch (ex: Exception) {
            val status = Status.fromThrowable(ex)

            when (status.code) {
                Status.Code.NOT_FOUND -> {
                    sender.sendMessage("§cRole does not exist.")
                }
                Status.Code.UNAVAILABLE -> {
                    sender.sendMessage("§cThe service is currently offline. Please try again later.")
                }
                else -> {
                    sender.sendMessage("§4There have been an error during the request. Please look into the log.")
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun permissionAdd(sender: CommandSender, name: String, permission: String) {
        try {
            val response = plugin.blockingStub.addRolePermission(
                AddRolePermissionRequest.newBuilder()
                    .setRoleId(name)
                    .addPermissions(permission)
                    .build()
            )

            sender.sendMessage("§aPermission added.")
        } catch (ex: Exception) {
            val status = Status.fromThrowable(ex)

            when (status.code) {
                Status.Code.NOT_FOUND -> {
                    sender.sendMessage("§cRole does not exist.")
                }
                Status.Code.UNAVAILABLE -> {
                    sender.sendMessage("§cThe service is currently offline. Please try again later.")
                }
                else -> {
                    sender.sendMessage("§4There have been an error during the request. Please look into the log.")
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun permissionRemove(sender: CommandSender, name: String, permission: String) {
        try {
            val response = plugin.blockingStub.removeRolePermission(
                RemoveRolePermissionRequest.newBuilder()
                    .setRoleId(name)
                    .addPermissions(permission)
                    .build()
            )

            sender.sendMessage("§aPermission removed.")
        } catch (ex: Exception) {
            val status = Status.fromThrowable(ex)

            when (status.code) {
                Status.Code.NOT_FOUND -> {
                    sender.sendMessage("§cRole does not exist.")
                }
                Status.Code.UNAVAILABLE -> {
                    sender.sendMessage("§cThe service is currently offline. Please try again later.")
                }
                else -> {
                    sender.sendMessage("§4There have been an error during the request. Please look into the log.")
                    ex.printStackTrace()
                }
            }
        }
    }


}
