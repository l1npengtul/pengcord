package net.pengtul.pengcord.util

import net.pengtul.pengcord.metrics.Metrics
import org.bukkit.plugin.java.JavaPlugin

class Stats(plugin: JavaPlugin, pluginId: Int?) {
    private var metrics: Metrics? = null

    init {
        pluginId?.let {
            metrics = Metrics(plugin, pluginId)
        }
    }
}