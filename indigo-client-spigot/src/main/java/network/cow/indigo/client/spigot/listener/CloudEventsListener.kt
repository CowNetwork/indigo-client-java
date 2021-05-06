package network.cow.indigo.client.spigot.listener

import network.cow.indigo.client.spigot.IndigoPlugin
import network.cow.indigo.client.spigot.callEvent
import network.cow.indigo.client.spigot.event.PermissionUpdateEvent
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
                plugin.roleCache.updateRoles(role)

                plugin.userCache.getUsersWithRole(role.name).forEach {
                    if (!it.component2().addRoles(listOf(role))) {
                        return
                    }

                    val player = Bukkit.getPlayer(it.component1()) ?: return
                    player.reloadPermissions(plugin.userCache)
                    callEvent(plugin, PermissionUpdateEvent(player, PermissionUpdateEvent.Action.ROLE_ADDED))
                }
            }
            RoleUpdateEvent.Action.ACTION_DELETED -> {
                plugin.roleCache.deleteRole(role.name)

                plugin.userCache.getUsersWithRole(role.name).forEach {
                    if (!it.component2().removeRoles(listOf(role.name))) {
                        return
                    }

                    val player = Bukkit.getPlayer(it.component1()) ?: return
                    player.reloadPermissions(plugin.userCache)
                    callEvent(plugin, PermissionUpdateEvent(player, PermissionUpdateEvent.Action.ROLE_REMOVED))
                }
            }
            RoleUpdateEvent.Action.ACTION_UPDATED -> {
                plugin.roleCache.updateRoles(role)

                plugin.userCache.getUsersWithRole(role.name).forEach {
                    if (!it.component2().updateRoles(listOf(role))) {
                        return
                    }

                    val player = Bukkit.getPlayer(it.component1()) ?: return
                    player.reloadPermissions(plugin.userCache)
                    callEvent(plugin, PermissionUpdateEvent(player, PermissionUpdateEvent.Action.ROLE_UPDATED))
                }
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

        val indigoUser = plugin.userCache.getUser(uuid) ?: return
        val player = Bukkit.getPlayer(uuid)!!

        when (event.action) {
            UserPermissionUpdateEvent.Action.ACTION_PERM_ADDED,
            UserPermissionUpdateEvent.Action.ACTION_PERM_REMOVED -> {
                player.sendMessage("§7Your custom permissions got updated, you fucking dumbass.")
                indigoUser.setCustomPermissions(user.customPermissionsList)

                player.reloadPermissions(plugin.userCache)
                callEvent(plugin, PermissionUpdateEvent(player,
                    if (event.action == UserPermissionUpdateEvent.Action.ACTION_PERM_ADDED)
                        PermissionUpdateEvent.Action.PERM_ADDED
                    else PermissionUpdateEvent.Action.PERM_REMOVED
                ))
            }
            UserPermissionUpdateEvent.Action.ACTION_ROLE_ADDED,
            UserPermissionUpdateEvent.Action.ACTION_ROLE_REMOVED -> {
                player.sendMessage("§7Your roles got updated, you fucking dumbass.")
                val rolesList = user.rolesList.mapNotNull { plugin.roleCache.getRoleByUuid(it.id) }
                indigoUser.setRoles(rolesList)

                player.reloadPermissions(plugin.userCache)
                callEvent(plugin, PermissionUpdateEvent(player,
                    if (event.action == UserPermissionUpdateEvent.Action.ACTION_ROLE_ADDED)
                        PermissionUpdateEvent.Action.ROLE_ADDED
                    else PermissionUpdateEvent.Action.ROLE_REMOVED
                ))
            }
            else -> {
                // we don't change anything
            }
        }
    }
}
