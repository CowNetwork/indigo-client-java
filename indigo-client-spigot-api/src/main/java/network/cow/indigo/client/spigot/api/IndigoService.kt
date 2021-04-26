package network.cow.indigo.client.spigot.api

import network.cow.grape.Service
import network.cow.mooapis.indigo.v1.Role
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author Tobias Büser
 */
interface IndigoService : Service {

    fun listRoles(): List<Role>

    fun getRole(roleName: String): Role?

    fun reloadCache()

    fun reloadCacheAsync(callback: () -> Unit)

    fun getUser(uuid: UUID): IndigoUser?

    fun getUserAsync(uuid: UUID): CompletableFuture<IndigoUser?>

    fun getUser(name: String): IndigoUser?

    fun getUserAsync(name: String): CompletableFuture<IndigoUser?>

}
