package net.pengtul.pengcord.main

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.*
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.plugin.Plugin

class PacketEvent: PacketListener {
    private val mitigationRegex = Regex("\\$\\{jndi:*.*}")
    override fun onPacketSending(event: PacketEvent?) {
        event?.let { packetEvent ->
            if (packetEvent.packet.strings.read(0).matches(mitigationRegex)) {
                packetEvent.isCancelled = true
                Main.serverLogger.warning("Mitigated chat packet from ${packetEvent.source}!")
            }
        }
    }

    override fun onPacketReceiving(event: PacketEvent?) {
        event?.let { packetEvent ->
            if (packetEvent.packet.strings.read(0).matches(mitigationRegex)) {
                packetEvent.isCancelled = true
                Main.serverLogger.warning("Mitigated chat packet from ${packetEvent.source}!")
            }
        }
    }

    override fun getSendingWhitelist(): ListeningWhitelist {
        return ListeningWhitelist.newBuilder()
            .priority(ListenerPriority.HIGHEST)
            .types(PacketType.Play.Client.CHAT, PacketType.Play.Client.CLIENT_COMMAND, PacketType.Play.Server.CHAT)
            .build()
    }

    override fun getReceivingWhitelist(): ListeningWhitelist {
        return ListeningWhitelist.newBuilder()
            .priority(ListenerPriority.HIGHEST)
            .types(PacketType.Play.Client.CHAT, PacketType.Play.Client.CLIENT_COMMAND, PacketType.Play.Server.CHAT)
            .build()
    }

    override fun getPlugin(): Plugin {
        return Main.pengcord
    }
}