package network.cow.indigo.client.spigot.cowmand

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

/**
 * @author Tobias Büser
 */
class SpigotCowmandExecutor(val command: Cowmand) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, argsArray: Array<out String>): Boolean {
        command.Executor(sender, Arguments(argsArray)).execute()
        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, argsArray: Array<out String>): List<String> {
        val list = command.Executor(sender, Arguments(argsArray)).tabComplete()
        return list
    }


}
