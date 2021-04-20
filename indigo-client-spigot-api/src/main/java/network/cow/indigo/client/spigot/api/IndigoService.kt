package network.cow.indigo.client.spigot.api

import network.cow.grape.Service
import network.cow.mooapis.indigo.v1.Role

/**
 * @author Tobias Büser
 */
interface IndigoService : Service {

    fun listRoles(): List<Role>

    fun getRole(roleId: String): Role

}
