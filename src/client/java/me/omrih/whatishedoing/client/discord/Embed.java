package me.omrih.whatishedoing.client.discord;

public class Embed {
    private final String title;
    private final Integer color;
    private String description;
    private String thumbnailUrl;
    private String timestamp;

    public Embed(String title, Integer color) {
        this.title = title;
        this.color = color;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setThumbnailUrl(String imageUrl) {
        this.thumbnailUrl = imageUrl;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public Integer getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
