package network.cow.indigo.client.spigot.listener

import io.grpc.Status
import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.api.IndigoUser
import network.cow.indigo.client.spigot.handleGrpc
import network.cow.indigo.client.spigot.reloadPermissions
import network.cow.indigo.client.spigot.runAsync
import network.cow.mooapis.indigo.v1.AddUserRolesRequest
import network.cow.mooapis.indigo.v1.GetUserRequest
import network.cow.mooapis.indigo.v1.GetUserResponse
import network.cow.mooapis.indigo.v1.RoleIdentifier
import network.cow.mooapis.indigo.v1.User
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @author Tobias Büser
 */
class PlayerListener(private val plugin: IndigoPlugin) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
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
                val defaultRoleName = plugin.indigoConfig.defaultRole
                val defaultRole = plugin.cache.getRole(defaultRoleName)

                if (defaultRole != null && plugin.indigoConfig.assignDefaultRole) {
                    val assignStatus = handleGrpc {
                        plugin.stub.addUserRoles(
                            AddUserRolesRequest.newBuilder()
                                .setUserAccountId(uniqueId.toString())
                                .addRoleIds(RoleIdentifier.newBuilder().setUuid(defaultRole.id).build())
                                .build()
                        )
                    }
                    if (!assignStatus.isOk()) {
                        plugin.logger.warning("Tried to assign default role to $uniqueId, but it failed.")
                    } else {
                        // only assign role if it succeeded
                        user = User.newBuilder()
                            .setAccountId(uniqueId.toString())
                            .addRoles(defaultRole)
                            .build()

                        plugin.logger.info("Assigned default role to $uniqueId.")
                    }
                }
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
    }

}
