package network.cow.indigo.client.spigot.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

/**
 * @author Tobias Büser
 */
class SupCommandExecutor(val command: SupCommand) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, argsArray: Array<out String>): Boolean {
        val executor = command.Executor(sender, Arguments(argsArray))
        executor.execute()
        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, argsArray: Array<out String>): List<String> {
        return emptyList()
    }


}
