package network.cow.indigo.client.spigot

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @author Tobias Büser
 */
class PlayerListener(val plugin: IndigoPlugin) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        /*plugin.asyncStub.getUserRoles(GetUserRolesRequest.newBuilder().setUserAccountId(player.uniqueId.toString()).build(),
                object : StreamObserver<GetUserRolesResponse> {
                    override fun onNext(p0: GetUserRolesResponse?) {
                        // received some response
                        if (p0 == null) {
                            return
                        }

                        p0.rolesList.forEach { player.sendMessage("Received role: $it") }
                    }

                    override fun onError(p0: Throwable?) {
                        // something went wrong ..
                        plugin.logger.warning(p0?.message)
                        player.sendMessage("§4Something went wrong during the request to get your roles.")
                    }

                    override fun onCompleted() {
                        // do nothing
                    }

                })*/
    }

}
