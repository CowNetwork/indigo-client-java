package network.cow.indigo.client.spigot.command

import io.grpc.Status
import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.createRoleIdentifierOf
import network.cow.indigo.client.spigot.handleGrpc
import network.cow.mooapis.indigo.v1.DeleteRoleRequest
import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class RolesDeleteCommand(val plugin: IndigoPlugin) : SupCommand() {

    override val label = "delete"
    override val usage = "<name>"

    override fun execute(sender: CommandSender, args: Arguments) {
        if (args.isEmpty()) {
            sender.sendMessage("§c/roles delete <name>")
            return
        }
        val name = args[0]!!

        val role = plugin.roleCache.getRole(name)
        if (role == null) {
            sender.sendMessage("§cRole does not exist.")
            return
        }

        val status = handleGrpc {
            plugin.stub.deleteRole(
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

    override fun tabComplete(sender: CommandSender, args: Arguments): List<String> {
        return plugin.roleCache.getRoles().map { it.name }
    }
}
