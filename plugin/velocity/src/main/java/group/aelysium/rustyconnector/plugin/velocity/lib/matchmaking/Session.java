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
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.rmi.AlreadyBoundException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Session implements ISession {
    protected final UUID uuid = UUID.randomUUID();;
    protected final Map<UUID, IMatchPlayer<IPlayerRank>> players;
    protected IRankedMCLoader mcLoader;
    protected final Settings settings;
    protected final RankRange rankRange;
    protected boolean frozen = false;

    public Session(IMatchPlayer<IPlayerRank> starter, Settings settings) {
        this.players = new ConcurrentHashMap<>(settings.max());
        this.players.put(starter.player().uuid(), starter);
        this.rankRange = new RankRange(starter.rank(), settings.variance());
        this.settings = settings;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public Settings settings() {
        return this.settings;
    }

    public Optional<IRankedMCLoader> mcLoader() {
        if(!this.active()) return Optional.empty();
        return Optional.of(this.mcLoader);
    }

    public RankRange range() {
        return this.rankRange;
    }

    public boolean active() {
        return this.mcLoader != null;
    }

    public boolean full() {
        return this.players.size() >= settings.max();
    }

    public int size() {
        return this.players.size();
    }

    public boolean frozen() {
        return this.frozen;
    }

    public void end(List<UUID> winners, List<UUID> losers) {
        RankedFamily family = (RankedFamily) this.mcLoader.family();

        family.matchmaker().remove(this);
        Family parent = family.parent();

        for (IMatchPlayer<IPlayerRank> matchPlayer : this.players.values()) {
            try {
                if(winners.contains(matchPlayer.player().uuid())) matchPlayer.markWin();
                if(losers.contains(matchPlayer.player().uuid())) matchPlayer.markLoss();
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
        this.players.values().forEach(matchPlayer -> matchPlayer.player().sendMessage(Component.text(reason, NamedTextColor.RED)));

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

    public Map<UUID, IMatchPlayer<IPlayerRank>> players() {
        return players;
    }

    public boolean leave(IMatchPlayer<IPlayerRank> matchPlayer) {
        if(this.players.remove(matchPlayer.player().uuid()) == null) return false;

        if(this.players.size() >= this.settings.min()) return true;

        this.implode("To many players left your game session so it had to be terminated. Sessions that are ended early won't penalize you.");

        return true;
    }

    public PlayerConnectable.Request join(IMatchPlayer<IPlayerRank> matchPlayer) {
        CompletableFuture<ConnectionResult> result = new CompletableFuture<>();
        PlayerConnectable.Request request = new PlayerConnectable.Request(matchPlayer.player(), result);

        if(this.players.containsKey(matchPlayer.player().uuid())) {
            result.complete(ConnectionResult.success(Component.text("You're already in this session!"), this.mcLoader));
            return request;
        }

        if(!this.rankRange.validate(matchPlayer.rank())) {
            result.complete(ConnectionResult.failed(Component.text("You're not the right rank to connect to this session!")));
            return request;
        }

        if(this.frozen) {
            result.complete(ConnectionResult.failed(Component.text("This session is already active and not accepting new players!")));
            return request;
        }

        this.players.put(matchPlayer.player().uuid(), matchPlayer);

        if(this.active())
            try {
                ConnectionResult r = this.mcLoader.connect(matchPlayer.player()).result().get(5, TimeUnit.SECONDS);
                if (!r.connected())
                    throw new RuntimeException("This exception should never see the light of day! It simply causes the catch block to trigger!");

                result.complete(ConnectionResult.success(Component.text("You've successfully connected to the session!"), null));
            } catch (Exception e) {
                this.players.remove(matchPlayer.player().uuid());
                result.complete(ConnectionResult.failed(Component.text("Failed to connect to the session!")));
            }
        else result.complete(ConnectionResult.success(Component.text("Connected to session! Your session is waiting to load into a server..."), null));

        // The result should already be complete by this point.
        return request;
    }

    public boolean contains(IMatchPlayer<IPlayerRank> matchPlayer) {
        return this.players.containsKey(matchPlayer.player().uuid());
    }
    public void start(IRankedMCLoader mcLoader) throws AlreadyBoundException {
        if(mcLoader.currentSession().isPresent()) throw new AlreadyBoundException("There's already a Session running on this MCLoader!");
        ((RankedMCLoader) mcLoader).connect(this);
        this.mcLoader = mcLoader;
        if(this.settings.shouldFreeze()) this.frozen = true;
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("uuid", new JsonPrimitive(this.uuid.toString()));

        JsonArray array = new JsonArray();
        players.values().forEach(matchPlayer -> {
            JsonObject playerObject = new JsonObject();

            playerObject.add("uuid", new JsonPrimitive(matchPlayer.player().uuid().toString()));

            Object rank = "null";
            try {
                rank = matchPlayer.rank();
            } catch (Exception ignore) {}

            playerObject.add("rank", new JsonPrimitive(rank.toString()));

            array.add(object);
        });
        object.add("players", array);

        return object;
    }
}
