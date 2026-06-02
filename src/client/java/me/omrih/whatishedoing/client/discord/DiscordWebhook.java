package me.omrih.whatishedoing.client.discord;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.omrih.whatishedoing.client.WhatIsHeDoingClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Used to do webhook stuff easily
 * <p>
 * Taken from <a href="https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb">GitHub</a>
 */
public class DiscordWebhook {
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final Gson GSON = new Gson();
    private final String url;
    private String content;
    private String username;
    private String avatarUrl;

    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void execute() throws IOException, URISyntaxException {
        execute(null);
    }

    public void execute(Embed embed) throws IOException, URISyntaxException {
        JsonObject jsonPayload = new JsonObject();

        HttpPost executeWebhook = new HttpPost(new URI(this.url));
        executeWebhook.addHeader("User-Agent", "Java-DiscordWebhookBuilder-WhatIsHeDoing");

        if (embed == null) {
            if (this.content == null) {
                throw new IllegalArgumentException("Content must be set");
            }
            jsonPayload.addProperty("content", this.content);
        } else {
            JsonObject embedJson = new JsonObject();
            embedJson.addProperty("title", embed.getTitle());
            embedJson.addProperty("color", embed.getColor());

            if (embed.getAttachment() != null) {
                embed.setImageUrl("attachment://attachment.png");
            }
            if (embed.getDescription() != null) {
                embedJson.addProperty("description", embed.getDescription());
            }
            if (embed.getThumbnailUrl() != null) {
                JsonObject thumbnail = new JsonObject();
                thumbnail.addProperty("url", embed.getThumbnailUrl());
                embedJson.add("thumbnail", thumbnail);
            }
            if (embed.getImageUrl() != null) {
                JsonObject image = new JsonObject();
                image.addProperty("url", embed.getImageUrl());
                embedJson.add("image", image);
            }
            if (embed.getTimestamp() != null) {
                embedJson.addProperty("timestamp", embed.getTimestamp());
            }

            JsonArray embeds = new JsonArray();
            embeds.add(embedJson);

            jsonPayload.add("embeds", embeds);
        }
        jsonPayload.addProperty("username", this.username);
        jsonPayload.addProperty("avatar_url", this.avatarUrl);
        jsonPayload.addProperty("tts", false);

        if (embed != null && embed.getAttachment() != null) {
            File image = File.createTempFile("attachment-", ".png");

            embed.getAttachment().writeToFile(image);

            HttpEntity requestEntity = MultipartEntityBuilder.create()
                    .addBinaryBody("file1", image, ContentType.IMAGE_PNG, "attachment.png")
                    .addTextBody("payload_json", GSON.toJson(jsonPayload), ContentType.APPLICATION_JSON)
                    .build();
            executeWebhook.setEntity(requestEntity);
        } else {
            executeWebhook.addHeader("Content-Type", "application/json");
            StringEntity requestEntity = new StringEntity(GSON.toJson(jsonPayload), ContentType.APPLICATION_JSON);
            executeWebhook.setEntity(requestEntity);
        }

        CloseableHttpResponse response = httpClient.execute(executeWebhook);
    }
}