package me.omrih.whatishedoing.client.integration;

import me.omrih.legiti.client.World;
import me.omrih.legiti.client.api.LegitiLibAPI;
import me.omrih.whatishedoing.client.EventHandler;
import me.omrih.whatishedoing.client.WhatIsHeDoingClient;
import me.omrih.whatishedoing.client.discord.Embed;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.time.Instant;

public class LegitimooseIntegration implements Integration {
    @Override
    public boolean onJoinWorld(ClientPacketListener handler, PacketSender sender, Minecraft client) {
        return true;
    }

    @Override
    public boolean onLeaveWorld(ClientPacketListener handler, Minecraft client) {
        return true;
    }

    public void onWorldJoined(World world) {
        Embed embed = new Embed("Joined a legitimoose world", 0x8A5F41);
        embed.setDescription(WhatIsHeDoingClient.getConfig().name + " joined **" + world.name() + "**" + " by " + "**" + world.owner() + "**");
        embed.setTimestamp(Instant.now().toString());
        EventHandler.getInstance().sendWebhook(embed);
    }

    public void onWorldLeft(World world) {
        Embed embed = new Embed("Left a legitimoose world", 0xED4245);
        embed.setDescription(WhatIsHeDoingClient.getConfig().name + " left **" + world.name() + "**" + " by " + "**" + world.owner() + "**");
        embed.setTimestamp(Instant.now().toString());
        EventHandler.getInstance().sendWebhook(embed);
    }
}
