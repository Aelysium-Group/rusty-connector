package group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ResolvableFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ResolvableServer;

import java.util.Optional;

public class ServerResidence implements IServerResidence {
    protected ResolvablePlayer player;
    protected ResolvableServer server;
    protected ResolvableFamily family;
    protected Long expiration;

    public ServerResidence(Player player, PlayerServer server, StaticFamily family, LiquidTimestamp expiration) {
        this.player = ResolvablePlayer.from(player);
        this.server = ResolvableServer.from(server);
        this.family = ResolvableFamily.from(family);

        if(expiration == null) this.expiration = null;
        else this.expiration = expiration.epochFromNow();
    }

    public Optional<Player> player() {
        return Tinder.get().velocityServer().getPlayer(this.player.uuid());
    }
    public ResolvablePlayer rawPlayer() {
        return this.player;
    }


    public Optional<PlayerServer> server() {
        return this.server.resolve();
    }
    public ResolvableServer rawServer() {
        return this.server;
    }

    public Optional<BaseFamily> family() {
        return this.family.resolve();
    }
    public ResolvableFamily rawFamily() {
        return this.family;
    }

    public Long expiration() {
        return expiration;
    }

    public void expiration(LiquidTimestamp expiration) {
        if(expiration == null) this.expiration = null;
        else this.expiration = expiration.epochFromNow();
    }

    @Override
    public String toString() {
        return "<ServerResidence username="+this.player.username()+" family="+this.family.name()+" server="+this.server.serverInfo().getName()+">";
    }
}