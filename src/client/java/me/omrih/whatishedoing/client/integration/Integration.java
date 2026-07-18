package me.omrih.whatishedoing.client.integration;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public interface Integration {
    /**
     * @return true to pass the event, triggering other integrations or the base event handler. false to cancel further handling.
     */
    boolean onJoinWorld(ClientPacketListener handler, PacketSender sender, Minecraft client);
    /**
     * @return true to pass the event, triggering other integrations or the base event handler. false to cancel further handling.
     */
    boolean onLeaveWorld(ClientPacketListener handler, Minecraft client);
}
