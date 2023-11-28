package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games;

import com.velocitypowered.api.proxy.Player;
import de.gesundkrank.jskills.*;
import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.RankedGameAssociatePacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.IRankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;

import java.rmi.ConnectException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RankedGame implements IRankedGame {
    protected List<RankedTeam> teams = new ArrayList<>();
    protected final GameInfo gameInfo = GameInfo.getDefaultGameInfo();
    protected UUID uuid = UUID.randomUUID();
    protected MCLoader server = null;
    protected boolean ended = false;

    public UUID uuid() {
        return this.uuid;
    }

    public MCLoader server() {
        return this.server;
    }

    public boolean ended() {
        return this.ended;
    }

    public List<RankablePlayer> players() {
        List<RankablePlayer> players = new ArrayList<>();
        this.teams.forEach(team -> players.add((RankablePlayer) team.players()));
        return players;
    }

    public void connectServer(MCLoader server) {
        Vector<com.velocitypowered.api.proxy.Player> kickedPlayers = new Vector<>();

        for (RankablePlayer rankablePlayer : this.players()) {
            try {
                Player player = rankablePlayer.player().resolve().orElseThrow();
                try {
                    server.directConnect(player);
                } catch (ConnectException e) {
                    kickedPlayers.add(player);
                }
            } catch (NoSuchElementException ignore) {
            } // Player isn't online, so it's not like we could message them anyway.
        }

        kickedPlayers.forEach(player -> {
            // player.sendMessage(VelocityLang.GAME_FOLLOW_KICKED);
        });

        RankedGameAssociatePacket message = (RankedGameAssociatePacket) new GenericPacket.Builder()
                .setType(PacketType.ASSOCIATE_RANKED_GAME)
                .setAddress(this.server.address())
                .setOrigin(PacketOrigin.PROXY)
                .setParameter(RankedGameAssociatePacket.ValidParameters.GAME_UUID, this.uuid().toString())
                .buildSendable();
        Tinder.get().services().messenger().connection().orElseThrow().publish(message);

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

        QuickSort.sort(this.teams);

        Collection<ITeam> teams = new ArrayList<>();
        int[] scores = new int[this.teams.size()];
        AtomicInteger i = new AtomicInteger(0);
        this.teams.forEach(team -> {
            teams.add(team.innerTeam());
            scores[i.getAndIncrement()] = team.rank();
        });

        TrueSkillCalculator.calculateNewRatings(this.gameInfo, teams, scores);

        this.ended = true;
    }

    public enum RankerType {
        /**
         * Represents 1v1 teams where every player is for themselves.
         */
        SOLO,

        /**
         * Represents NvN teams where teams compete against eachother.
         */
        CO_OP
    }

    public enum ScoringType {
        /**
         * Based off of racing: 1st, 2nd, 3rd, etc.
         */
        PLACEMENT,
        /**
         * Players that collect the most points win.
         */
        POINTS,
    }
}
