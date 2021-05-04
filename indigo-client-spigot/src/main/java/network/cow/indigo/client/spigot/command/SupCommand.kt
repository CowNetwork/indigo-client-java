package network.cow.indigo.client.spigot.command

import org.bukkit.command.CommandSender

/**
 * @author Tobias Büser
 */
abstract class SupCommand {

    abstract val label: String

    open val aliases = listOf<String>()
    open val usage = ""
    open val subCommands = listOf<SupCommand>()
    open val permission = ""

    /**
     * Only executes the command at this level. The args are already sliced.
     */
    abstract fun execute(sender: CommandSender, args: Arguments)

    /**
     * Only completes at this level. The args are already sliced.
     */
    open fun tabComplete(sender: CommandSender, args: Arguments) = emptyList<String>()

    inner class Executor(val sender: CommandSender, val args: Arguments) {

        fun execute() {
            if (permission.isNotEmpty() && !sender.hasPermission(permission)) {
                sender.sendMessage("§cNo permission dude.")
                return
            }

            if (args.isEmpty()) {
                execute(sender, args)
                return
            }
            if (subCommands.isEmpty()) {
                execute(sender, args)
                return
            }

            val arg = args[0]!!.toLowerCase()
            subCommands.forEach {
                if (it.label == arg || it.aliases.contains(arg)) {
                    it.execute(sender, args.slice(1))
                    return@execute
                }
            }
        }

    }

}

class Arguments(private val list: List<String>) {

    val size: Int; get() = list.size

    constructor(array: Array<out String>) : this(array.toList())

    fun isEmpty() = this.size == 0

    operator fun get(index: Int): String? {
        if (index >= list.size || index < 0) {
            return null
        }
        return list[index]
    }

    fun get(index: Int, default: String): String {
        return get(index) ?: default
    }

    fun slice(fromIndex: Int): Arguments {
        if (fromIndex < 0) return this
        if (fromIndex >= list.size) return Arguments(listOf())
        return Arguments(list.slice(fromIndex until list.size))
    }

    override fun toString(): String {
        return list.toString()
    }

}

fun main() {
    val args = Arguments(listOf("roles", "add", "Superioz", "admin"))
    println(args)

    val subArgs = args.slice(2)
    println(subArgs)
}
