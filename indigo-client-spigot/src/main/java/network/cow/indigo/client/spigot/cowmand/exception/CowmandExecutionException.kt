package network.cow.indigo.client.spigot.cowmand.exception

/**
 * @author Tobias Büser
 */
open class CowmandExecutionException(message: String, val type: Type) : Exception(message) {

    enum class Type {

        NO_PERMISSION,
        EXCEPTION_THROWN

    }

}
