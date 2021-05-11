package network.cow.indigo.client.spigot.listener

import io.grpc.Status
import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.api.IndigoUser
import network.cow.indigo.client.spigot.handleGrpc
import network.cow.indigo.client.spigot.reloadPermissions
import network.cow.indigo.client.spigot.runAsync
import network.cow.mooapis.indigo.v1.GetUserRequest
import network.cow.mooapis.indigo.v1.GetUserResponse
import network.cow.mooapis.indigo.v1.Role
import network.cow.mooapis.indigo.v1.User
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @author Tobias Büser
 */
class PlayerListener(private val plugin: IndigoPlugin) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val uniqueId = event.player.uniqueId

        runAsync {
            var response: GetUserResponse? = null
            val status = handleGrpc {
                response = plugin.stub.getUser(
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

            var user = response!!.user
            if (user.rolesList.isEmpty()) {
                val defaultRoles = mutableListOf<Role>()
                if (plugin.indigoConfig.assignDefaultRole) {
                    val defaultRoleName = plugin.indigoConfig.defaultRole
                    val defaultRole = plugin.cache.getRole(defaultRoleName)

                    defaultRole?.apply { defaultRoles.add(this) }
                }

                user = User.newBuilder()
                    .setAccountId(uniqueId.toString())
                    .addAllRoles(defaultRoles)
                    .build()
            } else {
                plugin.cache.updateRoles(*user.rolesList.toTypedArray())
            }

            val indigoUser = IndigoUser(uniqueId, user)
            plugin.cache.store(uniqueId, indigoUser)

            event.player.reloadPermissions()
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.cache.invalidate(player.uniqueId)

        /*val role = indigoUser.rolesList.maxByOrNull { it.priority } ?: return
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        val team = scoreboard.getTeam("${role.priority}_${role.id.take(12)}")

        if (team != null && team.hasEntry(event.player.name)) {
            team.removeEntry(event.player.name)
        }*/
    }

}
