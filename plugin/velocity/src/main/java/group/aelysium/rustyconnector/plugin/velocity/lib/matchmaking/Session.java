package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.RankedMCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.rmi.AlreadyBoundException;
import java.util.*;

public class Session implements ISession {
    protected final UUID uuid;
    protected final List<IMatchPlayer<IPlayerRank>> players;
    protected final IRankedMCLoader mcLoader;
    protected final Settings settings;

    private Session(UUID uuid, List<IMatchPlayer<IPlayerRank>> players, IRankedMCLoader mcLoader, Settings settings) {
        this.uuid = uuid;
        this.players = players;
        this.mcLoader = mcLoader;
        this.settings = settings;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public Settings settings() {
        return this.settings;
    }

    public IRankedMCLoader mcLoader() {
        return this.mcLoader;
    }

    public void end(List<UUID> winners, List<UUID> losers) {
        RankedFamily family = (RankedFamily) this.mcLoader.family();

        family.matchmaker().remove(this);
        Family parent = family.parent();

        for (IMatchPlayer<IPlayerRank> matchPlayer : this.players) {
            try {
                if(winners.contains(matchPlayer.player().uuid())) matchPlayer.rank().markWin();
                if(losers.contains(matchPlayer.player().uuid())) matchPlayer.rank().markLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                parent.connect(matchPlayer.player());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.mcLoader.unlock();
    }

    public void implode(String reason) {
        this.players.forEach(matchPlayer -> matchPlayer.player().sendMessage(Component.text(reason, NamedTextColor.RED)));

        Packet packet = Tinder.get().services().packetBuilder().newBuilder()
                .identification(BuiltInIdentifications.RANKED_GAME_IMPLODE)
                .sendingToMCLoader(this.uuid())
                .parameter(RankedGame.Imploded.Parameters.REASON, reason)
                .parameter(RankedGame.Imploded.Parameters.SESSION_UUID, this.uuid.toString())
                .build();
        Tinder.get().services().magicLink().connection().orElseThrow().publish(packet);
        this.mcLoader.unlock();

        this.end(List.of(), List.of());
    }

    public List<IMatchPlayer<IPlayerRank>> players() {
        return players;
    }

    public boolean leave(IMatchPlayer<IPlayerRank> player) {
        if(!this.players.remove(player)) return false;

        if(this.players.size() >= this.settings.min()) return true;

        this.implode("To many players left your game session so it had to be terminated. Sessions that are ended early won't penalize you.");

        return true;
    }

    public boolean contains(IMatchPlayer<IPlayerRank> player) {
        return this.players.contains(player);
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("uuid", new JsonPrimitive(this.uuid.toString()));

        JsonArray array = new JsonArray();
        players.forEach(matchPlayer -> {
            JsonObject playerObject = new JsonObject();

            playerObject.add("uuid", new JsonPrimitive(matchPlayer.player().uuid().toString()));

            Object rank = "null";
            try {
                rank = matchPlayer.rank().rank();
            } catch (Exception ignore) {}

            playerObject.add("rank", new JsonPrimitive(rank.toString()));

            array.add(object);
        });
        object.add("players", array);

        return object;
    }

    public static class Builder {
        protected List<IMatchPlayer<IPlayerRank>> players = new ArrayList<>();
        protected RankedMCLoader mcLoader;

        /**
         * Add a player to the match
         * @param player The player to add.
         */
        public void addPlayer(IMatchPlayer<IPlayerRank> player) {
            this.players.add(player);
        }

        /**
         * Builds the gamematch.
         * @return A {@link Session}, or `null` if there are still teams that aren't at least filled to the minimum.
         */
        public Session.Waiting build() {
            return new Session.Waiting(players);
        }
    }

    public static class Waiting implements ISession.IWaiting {
        protected UUID uuid = UUID.randomUUID();
        protected List<IMatchPlayer<IPlayerRank>> players;

        protected Waiting(List<IMatchPlayer<IPlayerRank>> teams) {
            this.players = teams;
        }

        public int size() {
            return this.players.size();
        }

        public UUID uuid() {
            return this.uuid;
        }

        public List<IMatchPlayer<IPlayerRank>> players() {
            return this.players;
        }

        public boolean remove(IMatchPlayer<IPlayerRank> player) {
            return this.players.remove(player);
        }

        /**
         * Starts the session on the specified MCLoader.
         * By the time {@link Session} is returned, it should be assumed that all players have connected.
         * @param mcLoader The MCLoader to run the session on.
         * @param settings The settings that govern this session.
         * @return A running {@link Session}.
         * @throws AlreadyBoundException If a session is already running on this MCLoader.
         */
        public ISession start(IRankedMCLoader mcLoader, Settings settings) throws AlreadyBoundException {
            ISession session = new Session(uuid, players, mcLoader, settings);
            if(mcLoader.currentSession().isPresent()) throw new AlreadyBoundException("There's already a Session running on this MCLoader!");
            ((RankedMCLoader) mcLoader).connect(session);
            return session;
        }

        @Override
        public boolean contains(IMatchPlayer<IPlayerRank> player) {
            return this.players.contains(player);
        }
    }
}
