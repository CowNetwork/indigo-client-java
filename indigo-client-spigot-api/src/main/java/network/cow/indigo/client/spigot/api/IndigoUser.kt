package network.cow.indigo.client.spigot.api

import network.cow.mooapis.indigo.v1.Role
import network.cow.mooapis.indigo.v1.User
import java.util.UUID

/**
 * @author Tobias Büser
 */
class IndigoUser(val uuid: UUID, private val user: User) {

    lateinit var permissions: PermissionList
    val roles: MutableList<Role> = user.rolesList.toMutableList()

    init {
        this.reloadPermissionList()
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
        if (roles.isEmpty()) {
            return null
        }
        return roles.filter { !it.transient }.maxByOrNull { it.priority }
    }

    /**
     * Returns a list of all [Role]s that are [Role.getTransient], sorted by
     * their priority descending.
     */
    fun getTransientRoles() = this.roles.filter { it.transient }.sortedByDescending { it.priority }

    fun hasPermission(perm: String) = this.permissions.hasPermission(perm)

    fun hasRole(roleName: String) = this.roles.find { it.name == roleName } != null

    fun setCustomPermissions(permissions: List<String>) {
        user.customPermissionsList.clear()
        user.customPermissionsList.addAll(permissions)

        this.reloadPermissionList()
    }

    fun setRoles(roles: List<Role>) {
        this.roles.clear()
        this.roles.addAll(roles)

        this.reloadPermissionList()
    }

    /**
     * Makes sure that given roles, if the user has them (by name),
     * will be updated, i.e. replacing these roles entirely.
     *
     * Returns true if the user at least has one role updated by that action.
     */
    fun updateRoles(roles: List<Role>): Boolean {
        var hasChanged = false
        roles.forEach {
            if (!hasRole(it.name)) return@forEach

            hasChanged = true
            val index = this.roles.indexOfFirst { role -> role.name == it.name }
            this.roles[index] = it
            return@forEach
        }

        if (hasChanged) {
            this.reloadPermissionList()
        }
        return hasChanged
    }

    /**
     * Makes sure that every role in [roles] is added
     * to the [User.getRolesList].
     *
     * Returns true if the list size actually increased.
     */
    fun addRoles(roles: List<Role>): Boolean {
        val beforeCount = user.rolesCount
        this.roles.addAll(roles)
        val afterCount = user.rolesCount

        this.reloadPermissionList()
        return afterCount > beforeCount
    }

    /**
     * Makes sure that every role in [roles] is removed
     * from the [User.getRolesList].
     *
     * Returns true if the list size actually decreased.
     */
    fun removeRoles(roles: List<String>): Boolean {
        val beforeCount = user.rolesCount
        this.roles.removeAll { roles.contains(it.name) }
        val afterCount = user.rolesCount

        this.reloadPermissionList()
        return afterCount < beforeCount
    }

    /**
     * Reloads the [permissions] list by using the backed [user]'s
     * roles and custom permissions.
     */
    private fun reloadPermissionList() {
        var prevPriority = 0
        var permissionList = PermissionList(listOf())
        this.roles.forEach { role ->
            val rolePermissionList = PermissionList(role.permissionsList)
            val superior = role.priority > prevPriority

            permissionList = permissionList.mergeWith(rolePermissionList, superior)

            prevPriority = role.priority
        }

        val customPermissionList = PermissionList(user.customPermissionsList)
        permissionList = permissionList.mergeWith(customPermissionList, true)

        this.permissions = permissionList.compacted()
    }

}
