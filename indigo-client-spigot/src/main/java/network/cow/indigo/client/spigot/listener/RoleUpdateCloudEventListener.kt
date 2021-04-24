package network.cow.indigo.client.spigot.listener

import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.mooapis.indigo.v1.RoleUpdateEvent

/**
 * @author Tobias Büser
 */
class RoleUpdateCloudEventListener(private val plugin: IndigoPlugin) : (RoleUpdateEvent) -> Unit {
    override fun invoke(event: RoleUpdateEvent) {
        val role = event.role ?: return

        when (event.action) {
            RoleUpdateEvent.Action.ACTION_ADDED -> {
                plugin.roleCache.updateRoles(role)
            }
            RoleUpdateEvent.Action.ACTION_DELETED -> {
                plugin.roleCache.deleteRole(role.name)
            }
            RoleUpdateEvent.Action.ACTION_UPDATED -> {
                plugin.roleCache.updateRoles(role)
            }
            else -> {
                // we don't change anything
            }
        }
    }
}
