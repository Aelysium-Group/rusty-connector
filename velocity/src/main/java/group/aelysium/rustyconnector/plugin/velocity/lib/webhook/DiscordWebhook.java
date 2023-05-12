package group.aelysium.rustyconnector.plugin.velocity.lib.webhook;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import group.aelysium.rustyconnector.plugin.velocity.lib.http.SimpleRequest;

import java.net.MalformedURLException;
import java.net.URL;

public class DiscordWebhook {
    private final String name;
    private final URL url;
    public DiscordWebhook(String name, URL url) {
        this.name = name;
        this.url = url;
    }
    public DiscordWebhook(String name, String url) throws MalformedURLException {
        this.name = name;
        this.url = new URL(url);
    }

    public String getName() {
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
