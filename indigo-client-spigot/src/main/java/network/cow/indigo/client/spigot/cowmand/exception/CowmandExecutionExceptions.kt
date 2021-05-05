package network.cow.indigo.client.spigot.cowmand.exception

import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
class CowmandNoPermissionException(val sender: CommandSender, val permission: String)
    : CowmandExecutionException("${sender.name} does not have permission $permission", Type.NO_PERMISSION)

class CowmandExceptionThrownException(val exception: Exception)
    : CowmandExecutionException("Exception during execution", Type.EXCEPTION_THROWN)
