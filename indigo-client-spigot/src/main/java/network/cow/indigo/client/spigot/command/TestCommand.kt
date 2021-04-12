package network.cow.indigo.client.spigot.command

import io.grpc.stub.StreamObserver
import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.mooapis.indigo.v1.GetUserRolesRequest
import network.cow.mooapis.indigo.v1.GetUserRolesResponse
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @author Tobias Büser
 */
class TestCommand(val plugin: IndigoPlugin) : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            return false
        }
        sender.sendMessage("Sending request ..")
        plugin.asyncStub.getUserRoles(GetUserRolesRequest.newBuilder().setUserAccountId(sender.uniqueId.toString()).build(),
                object : StreamObserver<GetUserRolesResponse> {
                    override fun onNext(p0: GetUserRolesResponse?) {
                        // received some response
                        if (p0 == null) {
                            return
                        }

                        p0.rolesList.forEach { sender.sendMessage("§aReceived role: $it") }
                    }

                    override fun onError(p0: Throwable?) {
                        // something went wrong ..
                        p0?.printStackTrace()
                        sender.sendMessage("§4Something went wrong during the request to get your roles.")
                    }

                    override fun onCompleted() {
                        // do nothing
                    }

                })
        return true
    }
}
