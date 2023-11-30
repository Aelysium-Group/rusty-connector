package group.aelysium.rustyconnector.plugin.fabric.central;

import cloud.commandframework.fabric.FabricServerCommandManager;
import com.mojang.authlib.GameProfile;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderFlame;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.core.lib.lang.config.RootLanguageConfig;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnection;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnector;
import group.aelysium.rustyconnector.core.mcloader.central.CoreServiceHandler;
import group.aelysium.rustyconnector.plugin.fabric.FabricRustyConnector;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Tinder extends MCLoaderTinder {
    private static Tinder instance;

    private final FabricServerCommandManager<CommandSource> commandManager;
    private final FabricRustyConnector plugin;
    private MCLoaderFlame<CoreServiceHandler, RedisConnection, RedisConnector> flame;
    private final PluginLogger pluginLogger;
    private final LangService lang;


    private Tinder(FabricRustyConnector plugin, PluginLogger logger, LangService lang) throws Exception {
        this.plugin = plugin;
        this.pluginLogger = logger;
        this.lang = lang;

        this.commandManager = null;

        instance = this;
    }

    public static Tinder get() {
        return instance;
    }

    /**
     * Ignites a {@link MCLoaderFlame} which effectively starts the RustyConnector kernel.
     */
    public void ignite() throws RuntimeException {
        this.flame = group.aelysium.rustyconnector.plugin.fabric.central.Flame.fabricateNew(this.lang);
    }

    /**
     * Restarts the entire RustyConnector kernel by exhausting the current {@link MCLoaderFlame} and igniting a new one.
     */
    public void rekindle() {
        this.flame.exhaust();
        this.flame = null;

        this.ignite();
    }

    @Override
    public PluginLogger logger() {
        return this.pluginLogger;
    }

    @Override
    public String dataFolder() {
        return FabricLoader.getInstance().getGameDir().toString();
    }

    @Override
    public LangService lang() {
        return this.lang;
    }

    public Path dataFolderPath() {
        return FabricLoader.getInstance().getGameDir();
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

    /**
     * Returns the currently active RustyConnector kernel.
     * @return A {@link MCLoaderFlame}.
     */
    public MCLoaderFlame flame() {
        return this.flame;
    }

    public CoreServiceHandler services() {
        return this.flame.services();
    }

    public FabricServerCommandManager<CommandSource> commandManager() {
        return commandManager;
    }

    /**
     * Creates new {@link Tinder} based on the gathered resources.
     */
    public static Tinder gather(FabricRustyConnector plugin, Logger logger) {
        PluginLogger pluginLogger = new group.aelysium.rustyconnector.plugin.fabric.PluginLogger(logger);
        try {
            RootLanguageConfig config = new RootLanguageConfig(new File(FabricLoader.getInstance().getConfigDir().toString(), "language.yml"));
            if (!config.generate(pluginLogger))
                throw new IllegalStateException("Unable to load or create language.yml!");
            config.register();

            LangService langService = LangService.resolveLanguageCode(config.getLanguage(), FabricLoader.getInstance().getConfigDir());

            return new Tinder(plugin, pluginLogger, langService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
