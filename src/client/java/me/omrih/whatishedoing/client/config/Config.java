package me.omrih.whatishedoing.client.config;

import me.shedaniel.autoconfig.ConfigData;

import java.util.ArrayList;
import java.util.List;

@me.shedaniel.autoconfig.annotation.Config(name = "whatishedoing")
public class Config implements ConfigData {
    public List<String> webhooks = List.of("https://canary.discord.com/api/webhooks/");

    public String name = "He";
}
