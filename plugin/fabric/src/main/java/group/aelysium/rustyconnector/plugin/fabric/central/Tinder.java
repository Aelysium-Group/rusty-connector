package group.aelysium.rustyconnector.plugin.fabric.central;

import cloud.commandframework.fabric.FabricServerCommandManager;
import com.mojang.authlib.GameProfile;
import group.aelysium.central.MCLoaderTinder;
import group.aelysium.rustyconnector.plugin.fabric.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.fabric.events.OnPlayerJoin;
import group.aelysium.rustyconnector.plugin.fabric.events.OnPlayerLeave;
import group.aelysium.rustyconnector.plugin.fabric.events.OnPlayerPreLogin;
import group.aelysium.rustyconnector.toolkit.common.logger.PluginLogger;
import group.aelysium.rustyconnector.core.common.lang.LangService;
import group.aelysium.rustyconnector.core.common.lang.config.RootLanguageConfig;
import group.aelysium.rustyconnector.plugin.fabric.FabricRustyConnector;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Tinder extends MCLoaderTinder {
    protected static Tinder instance;
    public static Tinder get() {
        return instance;
    }
    private final FabricServerCommandManager<CommandSource> commandManager;
    private final FabricRustyConnector plugin;

    protected Tinder(FabricRustyConnector plugin, PluginLogger logger, LangService lang) throws Exception {
        super(logger, lang);
        this.plugin = plugin;

        this.commandManager = null;

        instance = this;
    }

    @Override
    public String dataFolder() {
        return FabricLoader.getInstance().getGameDir().toString();
    }

    @Override
    public void setMaxPlayers(int max) {
        // Need to do lol
    }

    @Override
    public int onlinePlayerCount() {
        return plugin.getServer().getPlayerManager().getPlayerList().size();
    }

    @Override
    public UUID getPlayerUUID(String name) {
        Optional<GameProfile> profile = Objects.requireNonNull(plugin.getServer().getUserCache()).findByName(name);
        return profile.map(GameProfile::getId).orElse(null);
    }

    @Override
    public String getPlayerName(UUID uuid) {
        Optional<GameProfile> profile = Objects.requireNonNull(plugin.getServer().getUserCache()).getByUuid(uuid);
        return profile.map(GameProfile::getName).orElse(null);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return plugin.getServer().getPlayerManager().getPlayer(uuid) != null;
    }

    @Override
    public void teleportPlayer(UUID uuid, UUID targetUuid) {
        ServerPlayerEntity client = plugin.getServer().getPlayerManager().getPlayer(uuid);
        if (client == null) return;

        ServerPlayerEntity target = plugin.getServer().getPlayerManager().getPlayer(targetUuid);
        if (target == null) return;

       client.teleport(target.getServerWorld(), target.getX(), target.getY(), target.getZ(), 0 ,0);
    }

    @Override
    public void sendMessage(UUID uuid, Component component) {
        Objects.requireNonNull(plugin.getServer().getPlayerManager().getPlayer(uuid)).sendMessage(component);
    }

    /**
     * Get the paper server
     */
    public MinecraftServer fabricServer() {
        return this.plugin.getServer();
    }

    @Override
    public Object server() {
        return this.fabricServer();
    }

    public FabricServerCommandManager<CommandSource> commandManager() {
        return commandManager;
    }

    @Override
    public void ignite(int port) throws RuntimeException {
        super.ignite(port);

        CommandRusty.create(this.commandManager());

        OnPlayerJoin.register();
        OnPlayerLeave.register();
        OnPlayerPreLogin.register();
    }

    /**
     * Creates new {@link Tinder} based on the gathered resources.
     */
    public static Tinder gather(FabricRustyConnector plugin, Logger logger) {
        PluginLogger pluginLogger = new group.aelysium.rustyconnector.plugin.fabric.PluginLogger(logger);
        try {
            RootLanguageConfig config = RootLanguageConfig.construct(FabricLoader.getInstance().getConfigDir());

            LangService langService = LangService.resolveLanguageCode(config.getLanguage(), FabricLoader.getInstance().getConfigDir());

            return new Tinder(plugin, pluginLogger, langService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
