package network.cow.indigo.client.spigot.api.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * @author Tobias Büser
 */
class PermissionUpdateEvent(player: Player) : PlayerEvent(player) {

    companion object {
        private val handlerList = HandlerList()

        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

}
