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
class RoleCache(private val stub: IndigoServiceGrpc.IndigoServiceBlockingStub, private val plugin: JavaPlugin) {

    /**
     * Map to cache the roles with. We need it thread-safe
     * as it will be accessible from multiple threads at the same time.
     *
     * Mapping from [Role.getName] to [Role].
     */
    private var rolesMap = ConcurrentHashMap<String, Role>()

    fun reloadFromService() {
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
        updateEntries.addAll(this.updateRolesAndGetEventEntries(*newRolesList.toTypedArray()))
        if (updateEntries.isNotEmpty()) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                Bukkit.getPluginManager().callEvent(RolesUpdateEvent(updateEntries))
            })
        }

        plugin.logger.info("Loaded a total of ${rolesMap.size} roles.")
    }

    fun getRoles() = rolesMap.values

    fun getRole(name: String) = rolesMap[name]

    fun deleteRole(name: String) {
        val previousRole = rolesMap.remove(name) ?: return
        Bukkit.getPluginManager().callEvent(RolesUpdateEvent(listOf(
            RolesUpdateEvent.Entry(previousRole, RolesUpdateEvent.Action.REMOVE)
        )))
    }

    fun updateRoles(vararg roles: Role) {
        val updateEntries = this.updateRolesAndGetEventEntries(*roles)
        if (updateEntries.isNotEmpty()) {
            if (!Bukkit.isPrimaryThread()) {
                Bukkit.getScheduler().runTask(plugin, object : Runnable {
                    override fun run() {
                        Bukkit.getPluginManager().callEvent(RolesUpdateEvent(updateEntries))
                    }
                })
                return
            }
            Bukkit.getPluginManager().callEvent(RolesUpdateEvent(updateEntries))
        }
    }

    private fun updateRolesAndGetEventEntries(vararg roles: Role): List<RolesUpdateEvent.Entry> {
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
        val previousRole = rolesMap[role.name]
        rolesMap[role.name] = role

        if (previousRole == null) {
            return RolesUpdateEvent.Action.ADD
        } else if (previousRole != role) {
            return RolesUpdateEvent.Action.UPDATE
        }
        return null
    }

}
