package network.cow.indigo.client.spigot

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Tobias Büser
 */
private val threadPool: ExecutorService = Executors.newFixedThreadPool(3)

fun runAsync(exec: () -> Unit) {
    threadPool.execute(exec)
}
