package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub;

import com.google.gson.JsonObject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageTPAQueuePlayer;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.model.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.commands.CommandHub;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPACleaningService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPASettings;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.commands.CommandTPA;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.util.*;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.*;

public class HubService extends Service {
    private List<String> enabledFamilies;

    public HubService(List<String> enabledFamilies) {
        this.enabledFamilies = enabledFamilies;
    }

    public boolean isEnabled(String familyName) {
        return this.enabledFamilies.contains(familyName);
    }

    public void initCommand() {
        CommandManager commandManager = VelocityRustyConnector.getAPI().getServer().getCommandManager();
        if(!commandManager.hasCommand("hub"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("hub").build(),
                        CommandHub.create()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void kill() {

    }
}
