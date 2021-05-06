package network.cow.indigo.client.spigot

/**
 * @author Tobias Büser
 */
data class IndigoConfig(val defaultRole: String?, val assignDefaultRole: Boolean, val connection: Connection) {

    data class Connection(val host: String, val port: Int)

}
