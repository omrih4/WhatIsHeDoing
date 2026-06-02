package me.omrih.whatishedoing.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.omrih.whatishedoing.client.config.Config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhatIsHeDoingClient implements ClientModInitializer {
    public static EventHandler EVENT_HANDLER;
    private static final Logger LOGGER = LoggerFactory.getLogger("What Is He Doing?");
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            ResourceLocation.fromNamespaceAndPath("whatishedoing", "keymappings")
    );

    @Override
    public void onInitializeClient() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);

        KeyMapping sendScreenshotKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.whatishedoing.send_screenshot",
                        GLFW.GLFW_KEY_F9,
                        CATEGORY
                ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sendScreenshotKey.consumeClick()) {
                LOGGER.info("screenshot key pressed");
                EVENT_HANDLER.onTakeScreenshot();
            }
        });

        EVENT_HANDLER = new EventHandler();

        EVENT_HANDLER.onOpenGame();

        ClientPlayConnectionEvents.JOIN.register(EVENT_HANDLER::onJoinWorld);
        ClientPlayConnectionEvents.DISCONNECT.register(EVENT_HANDLER::onLeaveWorld);

        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> EVENT_HANDLER.onExitGame());
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Config getConfig() {
        return AutoConfig.getConfigHolder(Config.class).getConfig();
    }
}
