package me.omrih.whatishedoing.client;

import io.netty.channel.local.LocalAddress;
import me.omrih.legiti.LegitiLib;
import me.omrih.legiti.client.LegitiLibClient;
import me.omrih.legiti.client.api.event.WorldJoinedEvent;
import me.omrih.legiti.client.api.event.WorldLeftEvent;
import me.omrih.whatishedoing.client.discord.DiscordWebhook;
import me.omrih.whatishedoing.client.discord.Embed;
import me.omrih.whatishedoing.client.integration.Integration;
import me.omrih.whatishedoing.client.integration.LegitimooseIntegration;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventHandler {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("whatishedoing", "keymappings")
    );
    private static final KeyMapping sendScreenshotKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.whatishedoing.send_screenshot",
                    GLFW.GLFW_KEY_F9,
                    CATEGORY
            ));
    private static EventHandler INSTANCE;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<DiscordWebhook> webhooks = new ArrayList<>();
    private final List<Integration> integrations = List.of(new LegitimooseIntegration());

    private String prevIp = "";

    EventHandler() {
        for (String url : WhatIsHeDoingClient.getConfig().webhooks) {
            this.webhooks.add(new DiscordWebhook(url));
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sendScreenshotKey.consumeClick()) {
                EventHandler.getInstance().onTakeScreenshot();
            }
        });

        ClientPlayConnectionEvents.JOIN.register(this::onJoinWorld);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onLeaveWorld);

        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> onExitGame());

        // LegitimooseIntegration
        WorldJoinedEvent.EVENT.register((world) -> ((LegitimooseIntegration) integrations.getFirst()).onWorldJoined(world));
        WorldLeftEvent.EVENT.register((world) -> ((LegitimooseIntegration) integrations.getFirst()).onWorldLeft(world));
    }

    public static EventHandler getInstance() {
        if (INSTANCE == null) INSTANCE = new EventHandler();
        return INSTANCE;
    }

    public void sendWebhook(Embed embed) {
        reloadWebhooks();
        for (DiscordWebhook webhook : webhooks) {
            executorService.execute(() -> {
                try {
                    webhook.execute(embed);
                } catch (Exception e) {
                    WhatIsHeDoingClient.getLogger().error("failed to execute webhook", e);
                }
            });
        }
    }

    void onOpenGame() {
        Embed embed = new Embed("Opened Minecraft", 0x3498DB);
        embed.setDescription(WhatIsHeDoingClient.getConfig().name + " opened Minecraft");
        embed.setTimestamp(Instant.now().toString());
        sendWebhook(embed);
    }

    private void onExitGame() {
        Embed embed = new Embed("Closed Minecraft", 0x992D22);
        embed.setDescription(WhatIsHeDoingClient.getConfig().name + " closed Minecraft");
        embed.setTimestamp(Instant.now().toString());
        sendWebhook(embed);
    }

    private void onJoinWorld(ClientPacketListener handler, PacketSender sender, Minecraft client) {
        String joinType;
        String name;
        String prevprev = getPrevIp();
        if (handler.getConnection().getRemoteAddress() instanceof LocalAddress) {
            joinType = "world";
            name = client.getSingleplayerServer().getWorldData().getLevelName();
            setPrevIp(name);
            // Singleplayer world
        } else {
            String ip = ((InetSocketAddress) handler.getConnection().getRemoteAddress()).getAddress().getHostAddress();
            setPrevIp(ip);
            joinType = "server";
            name = client.getCurrentServer().name;
        }
        for (Integration integration : integrations) {
            boolean cont = integration.onJoinWorld(handler, sender, client);
            if (!cont) return;
        }
        if (prevprev.equals(getPrevIp())) return;
        Embed embed = new Embed("Joined a " + joinType, 0x57f287);
        embed.setDescription(WhatIsHeDoingClient.getConfig().name + " joined **" + name + "**");
        embed.setTimestamp(Instant.now().toString());
        sendWebhook(embed);
    }

    private void onLeaveWorld(ClientPacketListener handler, Minecraft client) {
        setPrevIp("");
        String joinType;
        String name;
        if (handler.getConnection().getRemoteAddress() instanceof LocalAddress) {
            joinType = "world";
            name = client.getSingleplayerServer().getWorldData().getLevelName();
            // Singleplayer world
        } else {
            joinType = "server";
            name = client.getCurrentServer().name;
        }
        for (Integration integration : integrations) {
            boolean cont = integration.onLeaveWorld(handler, client);
            if (!cont) return;
        }
        Embed embed = new Embed("Left a " + joinType, 0xED4245);
        embed.setDescription(WhatIsHeDoingClient.getConfig().name + " left **" + name + "**");
        embed.setTimestamp(Instant.now().toString());
        sendWebhook(embed);
    }

    private void onTakeScreenshot() {
        Screenshot.takeScreenshot(Minecraft.getInstance().gameRenderer.mainRenderTarget(), (screenshot) -> {
            Embed embed = new Embed("Took Screenshot", 0x3498DB);
            embed.setDescription(WhatIsHeDoingClient.getConfig().name + " took a screenshot");
            embed.setTimestamp(Instant.now().toString());
            embed.setAttachment(screenshot);
            sendWebhook(embed);
        });
    }

    private void reloadWebhooks() {
        if (webhooks.size() != WhatIsHeDoingClient.getConfig().webhooks.size()) {
            webhooks.clear();
            for (String url : WhatIsHeDoingClient.getConfig().webhooks) {
                webhooks.add(new DiscordWebhook(url));
            }
        }
    }


    /**
     * @return may be either an ip address or a singleplayer world name, or empty
     */
    public String getPrevIp() {
        return prevIp;
    }

    public void setPrevIp(String prevIp) {
        this.prevIp = prevIp;
    }
}
