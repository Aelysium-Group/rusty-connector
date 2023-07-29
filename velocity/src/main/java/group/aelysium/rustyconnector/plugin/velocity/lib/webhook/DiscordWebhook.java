package group.aelysium.rustyconnector.plugin.velocity.lib.webhook;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;

import java.net.MalformedURLException;
import java.net.URL;

public class DiscordWebhook {
    private final String name;
    private final URL url;
    public DiscordWebhook(String name, URL url) {
        this.name = name;
        this.url = url;
    }

    public String name() {
        return this.name;
    }

    /**
     * Fires the webhook.
     */
    public void fire(WebhookEmbed payload) {
        WebhookClient client = new WebhookClientBuilder(this.url.toString()).build();
        try {
            client.send(payload);
        } catch (Exception ignore) {}
    }
}
