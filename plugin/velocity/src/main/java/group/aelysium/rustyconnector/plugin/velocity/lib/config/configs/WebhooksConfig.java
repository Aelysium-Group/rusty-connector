package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhook;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookScope;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WebhooksConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.WebhooksConfig {
    protected WebhooksConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_WEBHOOKS_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(WebhooksConfig.class);
    }

    @SuppressWarnings("unchecked")
    protected void register() throws IllegalStateException {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        Boolean enabled = IYAML.getValue(this.data, "enabled", Boolean.class);
        if(!enabled) return;

        IYAML.get(this.data,"webhooks").childrenList().forEach(node -> {
            WebhookScope scope = WebhookScope.valueOf(IYAML.getValue(node, "scope", String.class).toUpperCase());
            String name = IYAML.getValue(node, "id", String.class);
            try {
                URL url = new URL(IYAML.getValue(node, "url", String.class));
                DiscordWebhook webhook = new DiscordWebhook(name, url);

                List<String> flags = IYAML.getValue(node, "flags", List.class);
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
                            String familyName = IYAML.getValue(node, "target-family", String.class);

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

    public static WebhooksConfig construct(Path dataFolder, LangService lang, ConfigService configService) {
        WebhooksConfig config = new WebhooksConfig(dataFolder, "extras/webhooks.yml", "webhooks", lang);
        config.register();
        configService.put(config);
        return config;
    }
}
