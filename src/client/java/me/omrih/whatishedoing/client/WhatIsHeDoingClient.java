package me.omrih.whatishedoing.client;

import me.omrih.whatishedoing.client.config.Config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhatIsHeDoingClient implements ClientModInitializer {
    public static EventHandler EVENT_HANDLER;
    private static final Logger LOGGER = LoggerFactory.getLogger("What Is He Doing?");

    @Override
    public void onInitializeClient() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        EVENT_HANDLER = new EventHandler();
        EVENT_HANDLER.onOpenGame();
        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> EVENT_HANDLER.onExitGame());
        ClientPlayConnectionEvents.JOIN.register(EVENT_HANDLER::onJoinWorld);
        ClientPlayConnectionEvents.DISCONNECT.register(EVENT_HANDLER::onLeaveWorld);
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Config getConfig() {
        return AutoConfig.getConfigHolder(Config.class).getConfig();
    }
}
