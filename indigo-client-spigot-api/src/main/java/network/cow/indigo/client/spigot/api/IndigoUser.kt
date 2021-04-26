package network.cow.indigo.client.spigot.api

import network.cow.mooapis.indigo.v1.Role
import network.cow.mooapis.indigo.v1.User

/**
 * @author Tobias Büser
 */
class IndigoUser(private val user: User) {

    val permissions: PermissionList

    init {
        var prevPriority = 0
        var permissionList = PermissionList(listOf())
        this.user.rolesList.forEach { role ->
            val rolePermissionList = PermissionList(role.permissionsList)
            val superior = role.priority > prevPriority

            permissionList = permissionList.mergeWith(rolePermissionList, superior)

            prevPriority = role.priority
        }

        val customPermissionList = PermissionList(user.customPermissionsList)
        permissionList = permissionList.mergeWith(customPermissionList, true)

        this.permissions = permissionList.compacted()
    }

    /**
     * Returns the [Role] that is not [Role.getTransient] and has the highest
     * [Role.getPriority].
     *
     * If the list of roles is empty, return null.
     * And if two or more roles have the same priority, we just return the
     * first occuring one, handled by [maxByOrNull].
     */
    fun getTopRole(): Role? {
        if (user.rolesList.isEmpty()) {
            return null
        }
        return user.rolesList.filter { !it.transient }.maxByOrNull { it.priority }
    }

    /**
     * Returns a list of all [Role]s that are [Role.getTransient], sorted by
     * their priority descending.
     */
    fun getTransientRoles() = user.rolesList.filter { it.transient }.sortedByDescending { it.priority }

    fun hasPermission(perm: String) = this.permissions.hasPermission(perm)

    fun getRoles(): List<Role> = this.user.rolesList

}
