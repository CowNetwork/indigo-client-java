package network.cow.indigo.client.spigot.command

import io.grpc.Status
import network.cow.cowmands.Arguments
import network.cow.cowmands.Cowmand
import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.createRole
import network.cow.indigo.client.spigot.handleGrpc
import network.cow.mooapis.indigo.v1.InsertRoleRequest
import network.cow.mooapis.indigo.v1.InsertRoleResponse
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesCreateCommand(val plugin: IndigoPlugin) : Cowmand() {

    override val label = "create"
    override val usage = "<name>"

    override fun execute(sender: CommandSender, args: Arguments) {
        if (args.isEmpty()) {
            sender.sendMessage("§c/roles create <name>")
            return
        }
        val name = args[0]

        val role = plugin.cache.getRole(name)
        if (role != null) {
            sender.sendMessage("§cRole already exists.")
            return
        }

        var response: InsertRoleResponse? = null
        val status = handleGrpc {
            response = plugin.stub.insertRole(
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

}
