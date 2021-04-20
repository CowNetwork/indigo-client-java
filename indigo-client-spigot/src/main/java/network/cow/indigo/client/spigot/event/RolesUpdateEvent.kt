package network.cow.indigo.client.spigot.event

import network.cow.mooapis.indigo.v1.Role
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * @author Tobias Büser
 */
class RolesUpdateEvent(val entries: List<Entry>) : Event() {

    companion object {
        private val handlerList = HandlerList()

        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    enum class Action {
        ADD, REMOVE, UPDATE
    }

    data class Entry(val role: Role, val action: Action)

}
