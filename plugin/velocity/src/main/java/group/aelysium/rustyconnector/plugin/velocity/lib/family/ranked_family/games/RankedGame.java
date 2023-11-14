package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games;

import de.gesundkrank.jskills.GameInfo;
import de.gesundkrank.jskills.IPlayer;
import de.gesundkrank.jskills.ITeam;
import de.gesundkrank.jskills.TrueSkillCalculator;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.RankedGameAssociatePacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.IRankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class RankedGame implements IRankedGame {
    protected final GameInfo gameInfo = GameInfo.getDefaultGameInfo();
    protected UUID uuid = UUID.randomUUID();
    protected PlayerServer server = null;
    protected boolean ended = false;

    public UUID uuid() {
        return this.uuid;
    }

    public PlayerServer server() {
        return this.server;
    }

    public boolean ended() {
        return this.ended;
    }

    public List<RankablePlayer> players() {
        return null;
    }

    protected abstract <TTeam extends ITeam> Collection<TTeam> teams();

    public void connectServer(PlayerServer server) {
        Tinder api = Tinder.get();

        RankedGameAssociatePacket message = (RankedGameAssociatePacket) new GenericPacket.Builder()
                .setType(PacketType.ASSOCIATE_RANKED_GAME)
                .setAddress(this.server.address())
                .setOrigin(PacketOrigin.PROXY)
                .setParameter(RankedGameAssociatePacket.ValidParameters.GAME_UUID, this.uuid().toString())
                .buildSendable();
        api.services().messenger().connection().orElseThrow().publish(message);

        this.server = server;
    }

    public void end() {
        Tinder api = Tinder.get();

        RankedGameAssociatePacket message = (RankedGameAssociatePacket) new GenericPacket.Builder()
                .setType(PacketType.ASSOCIATE_RANKED_GAME)
                .setAddress(this.server.address())
                .setOrigin(PacketOrigin.PROXY)
                .setParameter(RankedGameAssociatePacket.ValidParameters.GAME_UUID, "null")
                .buildSendable();
        api.services().messenger().connection().orElseThrow().publish(message);



        TrueSkillCalculator.calculateNewRatings(this.gameInfo, this.teams());

        this.ended = true;
    }
}
