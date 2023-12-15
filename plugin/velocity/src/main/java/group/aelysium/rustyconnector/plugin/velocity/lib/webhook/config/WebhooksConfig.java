package group.aelysium.rustyconnector.plugin.velocity.lib.webhook.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhook;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookScope;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.config.WhitelistConfig;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WebhooksConfig extends YAML {
    protected WebhooksConfig(Path dataFolder, String target, LangService lang) {
        super(dataFolder, target, lang, LangFileMappings.PROXY_WEBHOOKS_TEMPLATE);
    }

    @SuppressWarnings("unchecked")
    protected void register() throws IllegalStateException {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        Boolean enabled = this.getNode(this.data, "enabled", Boolean.class);
        if(!enabled) return;

        get(this.data,"webhooks").getChildrenList().forEach(node -> {
            WebhookScope scope = WebhookScope.valueOf(this.getNode(node, "scope", String.class).toUpperCase());
            String name = this.getNode(node, "id", String.class);
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

                            try {
                                new Family.Reference(familyName).get();
                            } catch (Exception ignore) {
                                logger.warn("webhooks.yml is pointing a webhook at a family with the id: " + familyName + ". No family with this id exists!");
                            }

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

    public static WebhooksConfig construct(Path dataFolder, LangService lang) {
        WebhooksConfig config = new WebhooksConfig(dataFolder, "extras/webhooks.yml", lang);
        config.register();
        return config;
    }
}
