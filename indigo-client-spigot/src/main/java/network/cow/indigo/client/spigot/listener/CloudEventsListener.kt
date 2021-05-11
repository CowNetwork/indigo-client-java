package network.cow.indigo.client.spigot.listener

import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.reloadPermissions
import network.cow.mooapis.indigo.v1.RoleUpdateEvent
import network.cow.mooapis.indigo.v1.UserPermissionUpdateEvent
import org.bukkit.Bukkit
import java.util.UUID

/**
 * @author Tobias Büser
 */
class RoleUpdateCloudEventListener(private val plugin: IndigoPlugin) : (RoleUpdateEvent) -> Unit {
    override fun invoke(event: RoleUpdateEvent) {
        val role = event.role ?: return

        when (event.action) {
            RoleUpdateEvent.Action.ACTION_ADDED -> {
                plugin.cache.addRoles(role)
            }
            RoleUpdateEvent.Action.ACTION_DELETED -> {
                plugin.cache.deleteRoles(role)
            }
            RoleUpdateEvent.Action.ACTION_UPDATED -> {
                plugin.cache.updateRoles(role)
            }
            else -> {
                // we don't change anything
            }
        }
    }
}

class UserPermissionUpdateCloudEventListener(private val plugin: IndigoPlugin) : (UserPermissionUpdateEvent) -> Unit {
    override fun invoke(event: UserPermissionUpdateEvent) {
        val user = event.user ?: return

        val uuid: UUID
        try {
            uuid = UUID.fromString(user.accountId)
        } catch (ex: IllegalArgumentException) {
            return
        }

        val indigoUser = plugin.cache.getUser(uuid) ?: return
        val player = Bukkit.getPlayer(uuid)!!

        when (event.action) {
            UserPermissionUpdateEvent.Action.ACTION_PERM_ADDED,
            UserPermissionUpdateEvent.Action.ACTION_PERM_REMOVED -> {
                indigoUser.setCustomPermissions(user.customPermissionsList)

                player.reloadPermissions()
            }
            UserPermissionUpdateEvent.Action.ACTION_ROLE_ADDED,
            UserPermissionUpdateEvent.Action.ACTION_ROLE_REMOVED -> {
                val rolesList = user.rolesList.mapNotNull { plugin.cache.getRoleByUuid(it.id) }
                indigoUser.setRoles(rolesList)

                player.reloadPermissions()
            }
            else -> {
                // we don't change anything
            }
        }
    }
}
