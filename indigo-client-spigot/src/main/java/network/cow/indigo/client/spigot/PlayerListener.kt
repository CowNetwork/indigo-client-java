package network.cow.indigo.client.spigot

import io.grpc.Status
import network.cow.indigo.client.spigot.permission.InjectedPermissibleBase
import network.cow.indigo.client.spigot.permission.injectPermissibleBase
import network.cow.mooapis.indigo.v1.GetUserRequest
import network.cow.mooapis.indigo.v1.GetUserResponse
import network.cow.mooapis.indigo.v1.User
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

/**
 * @author Tobias Büser
 */
class PlayerListener(private val plugin: IndigoPlugin) : Listener {

    private val indigoUsers = mutableMapOf<UUID, User>()

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val uniqueId = event.player.uniqueId

        runAsync {
            var response: GetUserResponse? = null
            val status = handleGrpc {
                response = plugin.blockingStub.getUser(
                    GetUserRequest.newBuilder().setUserAccountId(uniqueId.toString()).build()
                )
            }
            if (!status.isOk() || response == null) {
                status.handle(Status.Code.UNAVAILABLE) {
                    plugin.logger.warning("The service is unreachable.")
                }.handle {
                    // something went wrong ..
                    plugin.logger.warning("Something went wrong during Indigo request of $uniqueId")
                    status.error?.printStackTrace()
                }
                return@runAsync
            }

            val indigoUser = response!!.user
            plugin.roleCache.updateRolesAndFireEvent(*indigoUser.rolesList.toTypedArray())

            indigoUsers[uniqueId] = indigoUser

            injectPermissibleBase(event.player, InjectedPermissibleBase(event.player, indigoUser))
            event.player.updateCommands()

            // TODO indigo-scoreboards as a seperate plugin (and seperate github project)
            // create scoreboard team of role
            Bukkit.getScheduler().runTask(plugin, Runnable {
                val role = indigoUser.rolesList.maxByOrNull { it.priority }!!
                val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
                val team = scoreboard.getTeam("${role.priority}_${role.id.take(12)}")

                if (team != null && !team.hasEntry(event.player.name)) {
                    team.addEntry(event.player.name)
                }
            })
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val indigoUser = indigoUsers.remove(player.uniqueId) ?: return

        val role = indigoUser.rolesList.maxByOrNull { it.priority }!!
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        val team = scoreboard.getTeam("${role.priority}_${role.id.take(12)}")

        if (team != null && team.hasEntry(event.player.name)) {
            team.removeEntry(event.player.name)
        }
    }

}
