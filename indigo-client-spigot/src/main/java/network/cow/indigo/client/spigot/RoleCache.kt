package network.cow.indigo.client.spigot

import io.grpc.Status
import network.cow.indigo.client.spigot.event.RolesUpdateEvent
import network.cow.mooapis.indigo.v1.IndigoServiceGrpc
import network.cow.mooapis.indigo.v1.ListRolesRequest
import network.cow.mooapis.indigo.v1.ListRolesResponse
import network.cow.mooapis.indigo.v1.Role
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Tobias Büser
 */
class RoleCache(private val blockingStub: IndigoServiceGrpc.IndigoServiceBlockingStub, private val plugin: JavaPlugin) {

    /**
     * Map to cache the roles with. We need it thread-safe
     * as it will be accessible from multiple threads at the same time.
     */
    private var rolesMap = ConcurrentHashMap<String, Role>()

    fun reloadFromService() {
        plugin.logger.info("Loading roles...")
        var response: ListRolesResponse? = null
        val status = handleGrpc {
            response = this.blockingStub.listRoles(ListRolesRequest.newBuilder().build())
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

        val updateEntries = mutableListOf<RolesUpdateEvent.Entry>()

        // get difference in roles, i.e. all roles, that got removed
        val newRolesList = response!!.rolesList
        val differenceList = rolesMap.values - newRolesList
        if (differenceList.isNotEmpty()) {
            differenceList.forEach {
                updateEntries.add(RolesUpdateEvent.Entry(it, RolesUpdateEvent.Action.REMOVE))
            }
        }

        this.rolesMap.clear()
        updateEntries.addAll(this.updateRoles(*newRolesList.toTypedArray()))
        if (updateEntries.isNotEmpty()) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                Bukkit.getPluginManager().callEvent(RolesUpdateEvent(updateEntries))
            })
        }

        plugin.logger.info("Loaded a total of ${rolesMap.size} roles.")
    }

    fun getRoles() = rolesMap.values

    fun getRole(id: String) = rolesMap[id]

    fun updateRolesAndFireEvent(vararg roles: Role) {
        val updateEntries = this.updateRoles(*roles)
        if (updateEntries.isNotEmpty()) {
            Bukkit.getPluginManager().callEvent(RolesUpdateEvent(updateEntries))
        }
    }

    fun updateRolesSilently(vararg roles: Role) {
        this.updateRoles(*roles)
    }

    private fun updateRoles(vararg roles: Role): List<RolesUpdateEvent.Entry> {
        val updateEntries = mutableListOf<RolesUpdateEvent.Entry>()

        roles.forEach {
            val updateAction = updateRole(it)
            if (updateAction != null) {
                updateEntries.add(RolesUpdateEvent.Entry(it, updateAction))
            }
        }

        return updateEntries
    }

    private fun updateRole(role: Role): RolesUpdateEvent.Action? {
        val previousRole = rolesMap[role.id]
        rolesMap[role.id] = role

        if (previousRole == null) {
            return RolesUpdateEvent.Action.ADD
        } else if (previousRole != role) {
            return RolesUpdateEvent.Action.UPDATE
        }
        return null
    }

}
