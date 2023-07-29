package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhook;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookScope;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WebhooksConfig extends YAML {
    private static WebhooksConfig config;

    private WebhooksConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    public static WebhooksConfig getConfig() {
        return config;
    }

    public static WebhooksConfig newConfig(File configPointer, String template) {
        config = new WebhooksConfig(configPointer, template);
        return WebhooksConfig.getConfig();
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        Boolean enabled = this.getNode(this.data, "enabled", Boolean.class);
        if(!enabled) return;

        get(this.data,"webhooks").getChildrenList().forEach(node -> {
            WebhookScope scope = WebhookScope.valueOf(this.getNode(node, "scope", String.class).toUpperCase());
            String name = this.getNode(node, "name", String.class);
            try {
                URL url = new URL(this.getNode(node, "url", String.class));
                DiscordWebhook webhook = new DiscordWebhook(name, url);

                List<String> flags = this.getNode(node, "flags", List.class);
                List<WebhookAlertFlag> correctFlags = new ArrayList<>();
                for (String flag : flags) {
                    try {
                        WebhookAlertFlag correctFlag = WebhookAlertFlag.valueOf(flag.toUpperCase());
                        correctFlags.add(correctFlag);
                    } catch (IllegalArgumentException e) {
                        logger.warn("`"+flag+"` is not a proper webhook flag!");
                    }
                }

                for (WebhookAlertFlag flag : correctFlags) {
                    switch (scope) {
                        case PROXY -> WebhookEventManager.on(flag, webhook);
                        case FAMILY -> {
                            String familyName = this.getNode(node, "target-family", String.class);

                            BaseServerFamily family = api.services().familyService().find(familyName);
                            if (family == null)
                                logger.warn("webhooks.yml is pointing a webhook at a family with the name: " + familyName + ". No family with this name exists!");

                            WebhookEventManager.on(flag, familyName, webhook);
                        }
                    }
                }

                logger.log("Successfully registered the webhook: " + webhook.name() + "!");
            } catch (MalformedURLException e) {
                throw new IllegalStateException("`url` in webhooks.yml must be a valid url!");
            }
        });
    }
}
