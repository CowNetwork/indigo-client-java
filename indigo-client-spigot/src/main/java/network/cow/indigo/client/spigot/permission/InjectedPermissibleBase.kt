package network.cow.indigo.client.spigot.permission

import network.cow.indigo.client.spigot.api.IndigoUser
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissibleBase
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachmentInfo

/**
 * @author Tobias Büser
 */
class InjectedPermissibleBase(player: Player, private val indigoUser: IndigoUser) : PermissibleBase(player) {

    override fun isPermissionSet(name: String): Boolean {
        val list = this.indigoUser.permissions
        return list.contains(name)
    }

    override fun isPermissionSet(perm: Permission): Boolean {
        return this.isPermissionSet(perm.name)
    }

    override fun hasPermission(inName: String): Boolean {
        if (indigoUser.roles.isEmpty()) {
            // fall back to default permission check
            return super.hasPermission(inName)
        }

        return indigoUser.hasPermission(inName)
    }

    override fun hasPermission(perm: Permission): Boolean {
        return this.hasPermission(perm.name)
    }

    override fun recalculatePermissions() {
        // do nothing
    }

    override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
        val permissionAttachments = mutableSetOf<PermissionAttachmentInfo>()

        val list = this.indigoUser.permissions
        list.forEach {
            permissionAttachments.add(PermissionAttachmentInfo(this, it, null, true))
        }
        return permissionAttachments
    }

    override fun clearPermissions() {
        // do nothing
    }

}
