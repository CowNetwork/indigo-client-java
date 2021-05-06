package network.cow.indigo.client.spigot.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * @author Tobias Büser
 */
class PermissionUpdateEvent(player: Player, val action: Action) : PlayerEvent(player) {

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

        INJECTED,
        ROLE_ADDED,
        ROLE_REMOVED,
        ROLE_UPDATED,
        PERM_ADDED,
        PERM_REMOVED

    }

}
