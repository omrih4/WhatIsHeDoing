package me.omrih.whatishedoing.client;

import me.omrih.whatishedoing.client.config.Config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhatIsHeDoingClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("What Is He Doing?");

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Config getConfig() {
        return AutoConfig.getConfigHolder(Config.class).getConfig();
    }

    @Override
    public void onInitializeClient() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);

        EventHandler.getInstance().onOpenGame();
    }
}
