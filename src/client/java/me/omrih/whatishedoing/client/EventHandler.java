package me.omrih.whatishedoing.client;

import io.netty.channel.local.LocalAddress;
import me.omrih.whatishedoing.client.discord.DiscordWebhook;
import me.omrih.whatishedoing.client.discord.Embed;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventHandler {
    private final List<DiscordWebhook> webhooks = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public EventHandler() {
        for (String url : WhatIsHeDoingClient.getConfig().webhooks) {
            this.webhooks.add(new DiscordWebhook(url));
        }
    }

    public void onOpenGame() {
        reloadWebhooks();
        Embed embed = new Embed("Opened Minecraft", 0x3498DB);
        embed.setDescription(WhatIsHeDoingClient.getConfig().name + " opened Minecraft");
        embed.setTimestamp(Instant.now().toString());
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

    public void onExitGame() {
        reloadWebhooks();
        Embed embed = new Embed("Closed Minecraft", 0x992D22);
        embed.setDescription(WhatIsHeDoingClient.getConfig().name + " closed Minecraft");
        embed.setTimestamp(Instant.now().toString());
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

    public void onJoinWorld(ClientPacketListener handler, PacketSender sender, Minecraft client) {
        reloadWebhooks();
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
        Embed embed = new Embed("Joined a " + joinType, 0x57f287);
        embed.setDescription(WhatIsHeDoingClient.getConfig().name + " joined **" + name + "**");
        embed.setTimestamp(Instant.now().toString());
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

    public void onLeaveWorld(ClientPacketListener handler, Minecraft client) {
        reloadWebhooks();
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
        Embed embed = new Embed("Left a " + joinType, 0xED4245);
        embed.setDescription(WhatIsHeDoingClient.getConfig().name + " left **" + name + "**");
        embed.setTimestamp(Instant.now().toString());
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

    public void onTakeScreenshot() {
        Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget(), (screenshot) -> {
            Embed embed = new Embed("Took Screenshot", 0x3498DB);
            embed.setDescription(WhatIsHeDoingClient.getConfig().name + " took a screenshot");
            embed.setTimestamp(Instant.now().toString());
            embed.setAttachment(screenshot);
            for (DiscordWebhook webhook : webhooks) {
                executorService.execute(() -> {
                    try {
                        webhook.execute(embed);
                    } catch (Exception e) {
                        WhatIsHeDoingClient.getLogger().error("failed to execute webhook", e);
                    }
                });
            }
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
}
