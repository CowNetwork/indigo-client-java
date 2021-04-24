package network.cow.indigo.client.spigot

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import network.cow.cloudevents.CloudEventsService
import network.cow.grape.Grape
import network.cow.indigo.client.spigot.command.RolesCommand
import network.cow.indigo.client.spigot.listener.CloudEventsListener
import network.cow.indigo.client.spigot.listener.PlayerListener
import network.cow.mooapis.indigo.v1.IndigoServiceGrpc
import network.cow.mooapis.indigo.v1.RoleUpdateEvent
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color
import java.util.concurrent.TimeUnit

/**
 * @author Tobias Büser
 */
class IndigoPlugin : JavaPlugin() {

    private lateinit var channel: ManagedChannel
    lateinit var blockingStub: IndigoServiceGrpc.IndigoServiceBlockingStub

    lateinit var roleCache: RoleCache
    lateinit var indigoConfig: IndigoConfig

    override fun onEnable() {
        this.indigoConfig = IndigoConfig(
            this.config.getString("defaultGroup"),
            this.config.getBoolean("assignDefaultGroup")
        )

        this.channel = ManagedChannelBuilder.forAddress("localhost", 6969)
            .usePlaintext()
            .build()

        this.blockingStub = IndigoServiceGrpc.newBlockingStub(channel)

        val rolesCommand = RolesCommand(this)
        this.getCommand("roles")?.setExecutor(rolesCommand)
        this.getCommand("roles")?.tabCompleter = rolesCommand
        Bukkit.getPluginManager().registerEvents(PlayerListener(this), this)

        // load all roles and permission
        this.roleCache = RoleCache(blockingStub, this)
        this.roleCache.reloadFromService()

        // cloud events
        val service = Grape.getInstance()[CloudEventsService::class.java].getNow(null)
        if (service == null) {
            logger.warning("Could not find CloudEventsService, disabling ..")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        service.consumer.listen("cow.indigo.v1.RoleUpdateEvent", RoleUpdateEvent::class.java, CloudEventsListener(this))

        // TODO indigo-scoreboards as a seperate plugin (and seperate github project)
        roleCache.getRoles().forEach {
            val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
            val teamName = "${it.priority}_${it.id.take(12)}"

            var team = scoreboard.getTeam(teamName)
            team?.unregister()

            team = scoreboard.registerNewTeam(teamName)
            val color = it.color.toColor()
            val bukkitColor = NamedTextColor.nearestTo(TextColor.color(color.red, color.green, color.blue))

            team.color(NamedTextColor.GRAY)
            team.prefix(Component.text(it.id + " ", bukkitColor))
        }
    }

    override fun onDisable() {
        try {
            this.channel.shutdown().awaitTermination(2, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            logger.info("Error during shutdown, ignoring ..")
        }
    }

    private fun String.toColor() = Color(
        this.substring(0, 2).toInt(16),
        this.substring(2, 4).toInt(16),
        this.substring(4, 6).toInt(16)
    )

}
