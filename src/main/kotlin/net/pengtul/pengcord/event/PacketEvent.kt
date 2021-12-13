package net.pengtul.pengcord.event

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.*
import com.comphenix.protocol.events.PacketEvent
import net.pengtul.pengcord.main.Main
import org.bukkit.plugin.Plugin

class PacketEvent: PacketListener {
    private val mitigationRegex = Regex("\\$\\{jndi:*.*}")
    override fun onPacketSending(event: PacketEvent?) {
        event?.let { packetEvent ->
            if (packetEvent.packetType == PacketType.Play.Client.CHAT || packetEvent.packetType == PacketType.Play.Server.CHAT) {
                try {
                    if (packetEvent.packet.strings.read(0).matches(mitigationRegex)) {
                        packetEvent.isCancelled = true
                        Main.serverLogger.warning("Mitigated chat packet from ${packetEvent.player.name}!")
                    }
                } catch (_: Exception) {

                }
            }
        }
    }

    override fun onPacketReceiving(event: PacketEvent?) {
        event?.let { packetEvent ->
            if (packetEvent.packetType == PacketType.Play.Client.CHAT || packetEvent.packetType == PacketType.Play.Server.CHAT) {
                try {
                    if (packetEvent.packet.strings.read(0).matches(mitigationRegex)) {
                        packetEvent.isCancelled = true
                        Main.serverLogger.warning("Mitigated chat packet from ${packetEvent.player.name}!")
                    }
                } catch (_: Exception) {

                }
            }
        }
    }

    override fun getSendingWhitelist(): ListeningWhitelist {
        return ListeningWhitelist.newBuilder()
            .priority(ListenerPriority.HIGHEST)
            .types(PacketType.Play.Client.CHAT, PacketType.Play.Server.CHAT)
            .build()
    }

    override fun getReceivingWhitelist(): ListeningWhitelist {
        return ListeningWhitelist.newBuilder()
            .priority(ListenerPriority.HIGHEST)
            .types(PacketType.Play.Client.CHAT, PacketType.Play.Server.CHAT)
            .build()
    }

    override fun getPlugin(): Plugin {
        return Main.pengcord
    }
}