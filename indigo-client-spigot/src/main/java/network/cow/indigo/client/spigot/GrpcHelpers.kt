package network.cow.indigo.client.spigot

import com.google.protobuf.Message
import io.grpc.Status
import network.cow.mooapis.indigo.v1.Role
import network.cow.mooapis.indigo.v1.RoleIdentifier
import network.cow.mooapis.indigo.v1.RoleNameIdentifier
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

fun <T : Message> handleGrpcBetter(exec: () -> T): Pair<GrpcStatusHelper, T?> {
    try {
        val response = exec()
        return GrpcStatusHelper(Status.OK, null) to response
    } catch (ex: Exception) {
        val status = Status.fromThrowable(ex)
        if (status == Status.OK) {
            return GrpcStatusHelper(status, ex) to null
        }
        return GrpcStatusHelper(status, ex) to null
    }
}

private val THREAD_POOL: ExecutorService = Executors.newFixedThreadPool(3)

fun runAsync(exec: () -> Unit) {
    THREAD_POOL.execute(exec)
}

private const val ROLE_IDENTIFIER_TYPE = "minecraft"

fun createRoleIdentifierOf(roleName: String): RoleIdentifier {
    return RoleIdentifier.newBuilder().setNameId(
        RoleNameIdentifier.newBuilder()
            .setName(roleName)
            .setType(ROLE_IDENTIFIER_TYPE)
            .build()
    ).build()
}

fun createRole(name: String): Role {
    return Role.newBuilder()
        .setName(name)
        .setColor("")
        .setPriority(0)
        .setTransient(false)
        .setType(ROLE_IDENTIFIER_TYPE)
        .build()
}
