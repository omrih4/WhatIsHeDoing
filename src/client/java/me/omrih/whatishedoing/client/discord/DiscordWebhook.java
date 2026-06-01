package me.omrih.whatishedoing.client.discord;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Used to do webhook stuff easily
 * <p>
 * Taken from <a href="https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb">GitHub</a>
 */
public class DiscordWebhook {

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
        JsonObject json = new JsonObject();

        if (embed == null) {
            if (this.content == null) {
                throw new IllegalArgumentException("Content must be set");
            }
            json.addProperty("content", this.content);
        } else {
            JsonObject embedJson = new JsonObject();
            embedJson.addProperty("title", embed.getTitle());
            embedJson.addProperty("color", embed.getColor());

            if (embed.getDescription() != null) {
                embedJson.addProperty("description", embed.getDescription());
            }
            if (embed.getThumbnailUrl() != null) {
                JsonObject thumbnail = new JsonObject();
                thumbnail.addProperty("url", embed.getThumbnailUrl());
                embedJson.add("thumbnail", thumbnail);
            }
            if (embed.getTimestamp() != null) {
                embedJson.addProperty("timestamp", embed.getTimestamp());
            }

            JsonArray embeds = new JsonArray();
            embeds.add(embedJson);

            json.add("embeds", embeds);
        }
        json.addProperty("username", this.username);
        json.addProperty("avatar_url", this.avatarUrl);
        json.addProperty("tts", false);

        URL url = new URI(this.url).toURL();
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "Java-DiscordWebhookBuilder-LegitiDevs");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStream stream = connection.getOutputStream();
        stream.write(GSON.toJson(json).getBytes());
        stream.flush();
        stream.close();

        connection
                .getInputStream()
                .close(); // I'm not sure why but it doesn't work without getting the InputStream
        connection.disconnect();
    }
}