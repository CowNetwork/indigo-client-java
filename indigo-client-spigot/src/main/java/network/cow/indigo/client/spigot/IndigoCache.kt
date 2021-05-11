package network.cow.indigo.client.spigot

import io.grpc.Status
import network.cow.indigo.client.spigot.api.IndigoUser
import network.cow.indigo.client.spigot.api.event.RolesUpdateEvent
import network.cow.mooapis.indigo.v1.GetUserRequest
import network.cow.mooapis.indigo.v1.GetUserResponse
import network.cow.mooapis.indigo.v1.IndigoServiceGrpc
import network.cow.mooapis.indigo.v1.ListRolesRequest
import network.cow.mooapis.indigo.v1.ListRolesResponse
import network.cow.mooapis.indigo.v1.Role
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Tobias Büser
 */
class IndigoCache(private val stub: IndigoServiceGrpc.IndigoServiceBlockingStub, private val plugin: IndigoPlugin) {

    /**
     * Map to cache the roles with. We need it thread-safe
     * as it will be accessible from multiple threads at the same time.
     *
     * Mapping from [Role.getName] to [Role].
     */
    private var rolesMap = ConcurrentHashMap<String, Role>()

    /**
     * Mapping from [UUID] to [IndigoUser].
     */
    private var userMap = ConcurrentHashMap<UUID, IndigoUser>()

    /**
     * Calls the Indigo service and completely replaces all existing
     * [Role]s with the fetched list via [replaceRoles].
     * If the service is not reachable, a warning will be printed to console.
     */
    fun loadRolesFromService() {
        plugin.logger.info("Loading roles...")
        var response: ListRolesResponse? = null
        val status = handleGrpc {
            response = this.stub.listRoles(ListRolesRequest.newBuilder().build())
        }
        if (!status.isOk()) {
            status.handle(Status.Code.UNAVAILABLE) {
                plugin.logger.warning("Could not load roles because the service is offline.")
            }.handle {
                plugin.logger.warning("Could not load roles because of an error.")
                status.error?.printStackTrace()
            }
            return
        }

        this.replaceRoles(response!!.rolesList)

        plugin.logger.info("Loaded a total of ${rolesMap.size} roles.")
    }

    fun addRoles(vararg roles: Role) = this.updateRoles(*roles)

    fun getRoles() = rolesMap.values

    fun getRole(name: String?) = rolesMap[name]

    fun getRoleByUuid(uuid: String) = rolesMap.values.find { it.id == uuid }

    /**
     * Replaces all existing [Role]s with given list.
     * It will also detect if roles got deleted during this process.
     *
     * Also calls an [RolesUpdateEvent].
     */
    fun replaceRoles(newRoles: List<Role>) {
        val updateEntries = mutableListOf<RolesUpdateEvent.Entry>()

        val differenceList = rolesMap.values - newRoles
        if (differenceList.isNotEmpty()) {
            differenceList.forEach {
                updateEntries.add(RolesUpdateEvent.Entry(it, RolesUpdateEvent.Action.REMOVE))
            }
        }

        this.rolesMap.clear()
        newRoles.forEach {
            val updateAction = updateRole(it)
            if (updateAction != null) {
                updateEntries.add(RolesUpdateEvent.Entry(it, updateAction))
            }
        }

        this.callRolesUpdateEvent(updateEntries)
    }

    /**
     * Updates already existing [Role]s or adds new ones.
     *
     * Also calls an [RolesUpdateEvent] if something has changed.
     */
    fun updateRoles(vararg roles: Role) {
        val updateEntries = mutableListOf<RolesUpdateEvent.Entry>()
        roles.forEach {
            val updateAction = updateRole(it)
            if (updateAction != null) {
                updateEntries.add(RolesUpdateEvent.Entry(it, updateAction))
            }
        }

        this.callRolesUpdateEvent(updateEntries)
    }

    private fun updateRole(role: Role): RolesUpdateEvent.Action? {
        println("updateRole(${role.name})")
        val previousRole = rolesMap[role.name]
        rolesMap[role.name] = role

        println("=> Previous: ${previousRole?.name}: ${previousRole?.permissionsList}")
        println("=> New: ${role.name}: ${role.permissionsList}")
        println("=> Equals: ${previousRole == role}")

        if (previousRole == null) {
            return RolesUpdateEvent.Action.ADD
        } else if (previousRole != role) {
            return RolesUpdateEvent.Action.UPDATE
        }
        return null
    }

    /**
     * Deletes existing [Role]s.
     *
     * Also calls an [RolesUpdateEvent] if something has changed.
     */
    fun deleteRoles(vararg roles: Role) {
        val updateEntries = mutableListOf<RolesUpdateEvent.Entry>()

        roles.forEach {
            val previousRole = rolesMap.remove(it.name) ?: return@forEach

            updateEntries.add(RolesUpdateEvent.Entry(previousRole, RolesUpdateEvent.Action.REMOVE))
        }

        this.callRolesUpdateEvent(updateEntries)
    }

    fun loadUser(uuid: UUID): IndigoUser? {
        var response: GetUserResponse? = null
        val status = handleGrpc {
            response = stub.getUser(GetUserRequest.newBuilder()
                .setUserAccountId(uuid.toString())
                .build())
        }
        if (!status.isOk() || response == null) {
            return null
        }

        val user = response!!.user ?: return null
        return IndigoUser(uuid, user)
    }

    fun store(uuid: UUID, user: IndigoUser) {
        userMap[uuid] = user
    }

    fun getUser(uuid: UUID): IndigoUser? = userMap[uuid]

    fun getUsersWithRole(vararg roles: Role): List<IndigoUser> {
        return userMap.values.filter { user ->
            roles.any { user.hasRole(it.name) }
        }
    }

    private fun updateUsersRole(role: Role, action: RolesUpdateEvent.Action) {
        println("updateUsersRole(${role.name}, $action)")
        this.getUsersWithRole(role).forEach { user ->
            when (action) {
                RolesUpdateEvent.Action.ADD -> {
                    // ignore, that does not make sense
                }
                RolesUpdateEvent.Action.REMOVE -> {
                    if (!user.removeRoles(listOf(role.name))) {
                        return@forEach
                    }
                }
                RolesUpdateEvent.Action.UPDATE -> {
                    if (!user.updateRoles(listOf(role))) {
                        return@forEach
                    }
                }
            }
            user.reloadPermissions()
        }
    }

    fun invalidate(uuid: UUID) = userMap.remove(uuid)

    private fun callRolesUpdateEvent(entries: List<RolesUpdateEvent.Entry>) {
        if (entries.isEmpty()) return
        callEvent(plugin, RolesUpdateEvent(entries))
        entries.forEach { updateUsersRole(it.role, it.action) }
    }

}
