package group.aelysium.rustyconnector.plugin.velocity.lib.webhook;

import club.minnced.discord.webhook.send.WebhookEmbed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WebhookEventManager {
    private static Map<WebhookAlertFlag, List<DiscordWebhook>> proxyListeners = new HashMap<>();
    private static Map<WebhookAlertFlag, Map<String, List<DiscordWebhook>>> familyListeners = new HashMap<>();

    /**
     * Registers a webhook to the proxy.
     * @param webhook The webhook to register.
     */
    public static void on(WebhookAlertFlag flag, DiscordWebhook webhook) {
        proxyListeners.computeIfAbsent(flag, k -> new ArrayList<>()).add(webhook);
    }

    /**
     * Registers a webhook to a specific family.
     * @param webhook The webhook to register.
     */
    public static void on(WebhookAlertFlag flag, String family, DiscordWebhook webhook) {
        List<DiscordWebhook> listeners = familyListeners.computeIfAbsent(flag, k -> new HashMap<>()).computeIfAbsent(family, k -> new ArrayList<>());

        listeners.add(webhook);
    }

    /**
     * Fires a proxy alert.
     * @param flag The flag to fire.
     * @param payload The payload to send with the event.
     */
    public static CompletableFuture<Boolean> fire(WebhookAlertFlag flag, WebhookEmbed payload) {
        return CompletableFuture.supplyAsync(() -> {
            List<DiscordWebhook> listeners = proxyListeners.get(flag);
            if(listeners == null) return false;

            for (DiscordWebhook webhook : listeners)
                webhook.fire(payload);

            return true;
        });
    }

    /**
     * Fires a family alert.
     * @param flag The flag to fire.
     * @param familyName The family to target.
     * @param payload The payload to send with the event.
     */
    public static CompletableFuture<Boolean> fire(WebhookAlertFlag flag, String familyName, WebhookEmbed payload) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, List<DiscordWebhook>> families = familyListeners.get(flag);
            if (families == null) return false;

            List<DiscordWebhook> listeners = families.get(familyName);
            if (listeners == null) return false;

            for (DiscordWebhook webhook : listeners)
                webhook.fire(payload);

            return true;
        });
    }
}
