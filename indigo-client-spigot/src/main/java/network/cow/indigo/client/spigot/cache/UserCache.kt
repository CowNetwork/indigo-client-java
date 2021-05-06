package network.cow.indigo.client.spigot.cache

import network.cow.indigo.client.spigot.api.IndigoUser
import network.cow.indigo.client.spigot.handleGrpc
import network.cow.mooapis.indigo.v1.GetUserRequest
import network.cow.mooapis.indigo.v1.GetUserResponse
import network.cow.mooapis.indigo.v1.IndigoServiceGrpc
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Tobias Büser
 */
class UserCache(private val stub: IndigoServiceGrpc.IndigoServiceBlockingStub, private val plugin: JavaPlugin) {

    private var userMap = ConcurrentHashMap<UUID, IndigoUser>()

    fun getUser(uuid: UUID): IndigoUser? = userMap[uuid]

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
        return IndigoUser(user)
    }

    fun store(uuid: UUID, user: IndigoUser) {
        userMap[uuid] = user
    }

    fun invalidate(uuid: UUID) = userMap.remove(uuid)

    fun getUsersWithRole(roleName: String): List<Map.Entry<UUID, IndigoUser>> {
        return userMap.entries.filter { it.component2().hasRole(roleName) }
    }

}
