package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay;

import de.gesundkrank.jskills.ITeam;
import de.gesundkrank.jskills.TrueSkillCalculator;
import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.RankedGameAssociatePacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;

import java.rmi.ConnectException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Session {
    protected final UUID uuid = UUID.randomUUID();
    protected final List<Team> teams;
    protected MCLoader.Reference server = null;

    protected Session(List<Team> teams) {
        this.teams = teams;
    }

    public void connect(MCLoader server) {
        List<com.velocitypowered.api.proxy.Player> kickedPlayers = new Vector<>();

        for (RankedPlayer<?> rankedPlayer : this.players()) {
            try {
                com.velocitypowered.api.proxy.Player player = new Player.Reference(rankedPlayer.uuid()).get().resolve().orElseThrow();
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
                .setAddress(server.address())
                .setOrigin(PacketOrigin.PROXY)
                .setParameter(RankedGameAssociatePacket.ValidParameters.GAME_UUID, uuid.toString())
                .buildSendable();
        Tinder.get().services().messenger().connection().orElseThrow().publish(message);

        this.server = new MCLoader.Reference(server.serverInfo());
    }



    public void end() {
        /*
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

        this.ended = true;*/
    }

    public List<RankedPlayer<?>> players() {
        List<RankedPlayer<?>> players = new ArrayList<>();

        this.teams.forEach(team -> players.addAll(team.players()));

        return players;
    }

    public static class Builder {
        protected List<Team> teams = new ArrayList<>();

        public Builder teams(List<Team.Settings> settings) {
            settings.forEach(team -> this.teams.add(new Team(team, new Vector<>())));

            return this;
        }

        /**
         * Add a player to the match
         * @param player The player to add.
         * @return `true` if the player was added successfully. `false` otherwise.
         */
        public boolean addPlayer(RankedPlayer player) {
            for (Team team : teams)
                if(team.add(player)) return true;

            return false;
        }

        /**
         * Builds the gamematch.
         * @return A {@link Session}, or `null` if there are still teams that aren't at least filled to the minimum.
         */
        public Session build() {
            for (Team team : teams)
                if(!team.satisfactory()) return null;

            return new Session(teams);
        }
    }
}
