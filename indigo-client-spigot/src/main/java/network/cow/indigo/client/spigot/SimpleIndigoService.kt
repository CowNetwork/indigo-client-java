package network.cow.indigo.client.spigot

import network.cow.indigo.client.spigot.api.IndigoService
import network.cow.indigo.client.spigot.api.IndigoUser
import network.cow.mooapis.indigo.v1.Role
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author Tobias Büser
 */
class SimpleIndigoService(private val plugin: IndigoPlugin) : IndigoService {

    override fun listRoles(): List<Role> {
        return plugin.roleCache.getRoles().toList()
    }

    override fun getRole(roleName: String): Role? {
        return plugin.roleCache.getRole(roleName)
    }

    override fun reloadCache() {
        plugin.roleCache.reloadFromService()
    }

    override fun reloadCacheAsync(callback: () -> Unit) {
        runAsync {
            reloadCache()
            callback()
        }
    }

    override fun getUser(uuid: UUID): IndigoUser? {
        val user = plugin.userCache.getUser(uuid)
        if (user != null) {
            return user
        }
        return plugin.userCache.loadUser(uuid)
    }

    override fun getUser(name: String): IndigoUser? {
        TODO("Not yet implemented")
    }

    override fun getUserAsync(uuid: UUID): CompletableFuture<IndigoUser?> {
        val future = CompletableFuture<IndigoUser?>()
        runAsync {
            val user = getUser(uuid)
            future.complete(user)
        }
        return future
    }

    override fun getUserAsync(name: String): CompletableFuture<IndigoUser?> {
        val future = CompletableFuture<IndigoUser?>()
        runAsync {
            val user = getUser(name)
            future.complete(user)
        }
        return future
    }

}
