package network.cow.indigo.client.spigot.permission

import network.cow.mooapis.indigo.v1.User
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissibleBase
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachmentInfo

/**
 * @author Tobias Büser
 */
class InjectedPermissibleBase(private val player: Player, private val indigoUser: User) : PermissibleBase(player) {

    override fun isPermissionSet(name: String): Boolean {
        val list = this.indigoUser.getAllPermissions()
        return list.contains(name)
    }

    override fun isPermissionSet(perm: Permission): Boolean {
        return this.isPermissionSet(perm.name)
    }

    override fun hasPermission(inName: String): Boolean {
        val permList = PermissionList(this.indigoUser.getAllPermissions())
        return permList.hasPermission(inName)
    }

    override fun hasPermission(perm: Permission): Boolean {
        return this.hasPermission(perm.name)
    }

    override fun recalculatePermissions() {
        // do nothing
    }

    override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
        val permissionAttachments = mutableSetOf<PermissionAttachmentInfo>()

        val list = this.indigoUser.getAllPermissions()
        list.forEach {
            permissionAttachments.add(PermissionAttachmentInfo(this, it, null, true))
        }
        return permissionAttachments
    }

    override fun clearPermissions() {
        // do nothing
    }

}

// TODO adjust method so that priority/transience get used as well
fun User.getAllPermissions(): List<String> {
    val list = mutableListOf<String>()

    this.customPermissionsList.forEach { list.add(it) }
    this.rolesList.forEach { role ->
        role.permissionsList.forEach {
            list.add(it)
        }
    }
    return list
}
