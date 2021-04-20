package network.cow.indigo.client.spigot

import io.grpc.Status
import org.bukkit.command.CommandSender
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Tobias Büser
 */
class GrpcStatusHelper(private val status: Status, val error: Exception?) {

    private var executed = false

    fun isOk() = status.isOk

    fun handle(exec: () -> Unit): GrpcStatusHelper {
        if (!executed) {
            this.handle(status.code, exec)
        }
        return this
    }

    fun handle(code: Status.Code, exec: () -> Unit): GrpcStatusHelper {
        if (!executed && status.code == code) {
            exec()
            this.executed = true
        }
        return this
    }

    fun handleCommandDefault(sender: CommandSender): GrpcStatusHelper {
        this.handle(Status.Code.UNAVAILABLE) {
            sender.sendMessage("§cThe service is currently offline. Please try again later.")
        }.handle {
            sender.sendMessage("§4There has been an error during the request. Please look into the log.")
            error?.printStackTrace()
        }
        return this
    }

}

/**
 * A helper method to wrap grpc methods with a try-catch
 * to better handle with errors during the request.
 */
fun handleGrpc(exec: () -> Unit): GrpcStatusHelper {
    try {
        exec()
        return GrpcStatusHelper(Status.OK, null)
    } catch (ex: Exception) {
        val status = Status.fromThrowable(ex)
        if (status == Status.OK) {
            return GrpcStatusHelper(status, ex)
        }
        return GrpcStatusHelper(status, ex)
    }
}

private val THREAD_POOL: ExecutorService = Executors.newFixedThreadPool(3)

fun runAsync(exec: () -> Unit) {
    THREAD_POOL.execute(exec)
}
