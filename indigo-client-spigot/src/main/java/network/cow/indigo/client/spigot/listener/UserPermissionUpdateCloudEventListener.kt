package network.cow.indigo.client.spigot.listener

import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.mooapis.indigo.v1.UserPermissionUpdateEvent
import java.util.UUID

/**
 * @author Tobias Büser
 */
class UserPermissionUpdateCloudEventListener(private val plugin: IndigoPlugin) : (UserPermissionUpdateEvent) -> Unit {
    override fun invoke(event: UserPermissionUpdateEvent) {
        val user = event.user ?: return

        val uuid: UUID
        try {
            uuid = UUID.fromString(user.accountId)
        } catch (ex: IllegalArgumentException) {
            return
        }

        val indigoUser = plugin.userCache.getUser(uuid) ?: return


        when (event.action) {
            UserPermissionUpdateEvent.Action.ACTION_PERM_ADDED,
            UserPermissionUpdateEvent.Action.ACTION_PERM_REMOVED -> {
                indigoUser.setCustomPermissions(user.customPermissionsList)
            }
            UserPermissionUpdateEvent.Action.ACTION_ROLE_ADDED,
            UserPermissionUpdateEvent.Action.ACTION_ROLE_REMOVED -> {
                val rolesList = user.rolesList.mapNotNull { plugin.roleCache.getRoleByUuid(it.id) }
                indigoUser.setRoles(rolesList)
            }
            else -> {
                // we don't change anything
            }
        }
    }
}
