package network.cow.indigo.client.spigot

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import network.cow.indigo.client.spigot.command.TestCommand
import network.cow.mooapis.indigo.v1.IndigoServiceGrpc
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

/**
 * @author Tobias Büser
 */
class IndigoPlugin : JavaPlugin() {

    private lateinit var channel: ManagedChannel
    lateinit var blockingStub: IndigoServiceGrpc.IndigoServiceBlockingStub
    lateinit var asyncStub: IndigoServiceGrpc.IndigoServiceStub

    override fun onEnable() {
        this.channel = ManagedChannelBuilder.forAddress("localhost", 6969).usePlaintext().build()
        this.blockingStub = IndigoServiceGrpc.newBlockingStub(channel)
        this.asyncStub = IndigoServiceGrpc.newStub(channel)

        this.getCommand("test")?.setExecutor(TestCommand(this))
        Bukkit.getPluginManager().registerEvents(PlayerListener(this), this)
    }

    override fun onDisable() {
        this.channel.shutdown().awaitTermination(2, TimeUnit.SECONDS)
    }

}
