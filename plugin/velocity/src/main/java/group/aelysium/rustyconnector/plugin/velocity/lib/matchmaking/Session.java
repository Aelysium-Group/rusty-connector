package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.RankedMCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.rmi.AlreadyBoundException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Session implements ISession {
    protected final IMatchmaker matchmaker;
    protected final UUID uuid = UUID.randomUUID();
    protected final Map<UUID, IMatchPlayer> players;
    protected Set<UUID> previousPlayers;
    protected IRankedMCLoader mcLoader;
    protected final Settings settings;
    protected boolean ended = false;
    protected boolean frozen = false;

    public Session(IMatchmaker matchmaker, Settings settings) {
        this.matchmaker = matchmaker;
        this.players = new ConcurrentHashMap<>(settings.max());
        this.settings = settings;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public Settings settings() {
        return this.settings;
    }

    public boolean ended() {
        return this.ended;
    }

    public IMatchmaker matchmaker() {
        return this.matchmaker;
    }

    public Optional<IRankedMCLoader> mcLoader() {
        if(!this.active()) return Optional.empty();
        return Optional.of(this.mcLoader);
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

    public Map<UUID, IMatchPlayer> players() {
        return players;
    }

    public Set<UUID> previousPlayers() {
        if(this.previousPlayers == null) return new HashSet<>();
        return this.previousPlayers;
    }

    public boolean contains(IMatchPlayer matchPlayer) {
        return this.players.containsKey(matchPlayer.player().uuid());
    }

    public void empty() {
        this.players.clear();
    }

    protected void recordLeavingPlayer(UUID uuid) {
        if(this.previousPlayers == null) this.previousPlayers = new HashSet<>();
        this.previousPlayers.add(uuid);
    }

    public void start(IRankedMCLoader mcLoader) throws AlreadyBoundException {
        if(mcLoader.currentSession().isPresent()) throw new AlreadyBoundException("There's already a Session running on this MCLoader!");
        ((RankedMCLoader) mcLoader).connect(this);
        this.mcLoader = mcLoader;

        if(this.settings.shouldFreeze()) this.frozen = true;
    }

    public PlayerConnectable.Request join(IMatchPlayer matchPlayer) {
        CompletableFuture<ConnectionResult> result = new CompletableFuture<>();
        PlayerConnectable.Request request = new PlayerConnectable.Request(matchPlayer.player(), result);

        if(this.players.containsKey(matchPlayer.player().uuid())) {
            result.complete(ConnectionResult.success(Component.text("You're already in this session!"), this.mcLoader));
            return request;
        }

        if(this.frozen) {
            result.complete(ConnectionResult.failed(Component.text("This session is already active and not accepting new players!")));
            return request;
        }

        if(this.full()) {
            result.complete(ConnectionResult.failed(Component.text("This session is already full!")));
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

    public void leave(IPlayer player) {
        this.players.remove(player.uuid());

        this.recordLeavingPlayer(player.uuid());

        if(this.ended) return;

        if(settings.quittersLose()) {
            Optional<IMatchPlayer> matchPlayer = this.matchmaker.matchPlayer(player);
            matchPlayer.ifPresent(mp -> mp.gameRank().computor().compute(List.of(), List.of(mp), matchmaker, this));
        }

        if(this.players.size() > this.settings.min()) return;
        this.implode("To many players left your game session so it had to be terminated. Sessions that are ended early won't penalize you.");
    }

    public void implode(String reason, boolean unlock) {
        this.players.values().forEach(matchPlayer -> matchPlayer.player().sendMessage(Component.text(reason, NamedTextColor.RED)));

        if(this.active()) {
            Packet packet = Tinder.get().services().packetBuilder().newBuilder()
                    .identification(BuiltInIdentifications.RANKED_GAME_IMPLODE)
                    .sendingToMCLoader(this.mcLoader.uuid())
                    .parameter(RankedGame.Imploded.Parameters.REASON, reason)
                    .parameter(RankedGame.Imploded.Parameters.SESSION_UUID, this.uuid.toString())
                    .build();
            Tinder.get().services().magicLink().connection().orElseThrow().publish(packet);

            this.mcLoader.dropSession();
        }

        List<UUID> winners = new ArrayList<>();
        if(settings.stayersWin()) winners = new ArrayList<>(this.players.keySet());

        this.end(winners, List.of(), unlock);
    }

    public void implode(String reason) {
        this.implode(reason, true);
    }

    public void end(List<UUID> winners, List<UUID> losers, boolean unlock) {
        this.ended = true;

        ((Matchmaker) this.matchmaker).remove(this);

        IVelocityPlayerRank.IComputor computer = null;
        List<IMatchPlayer> playerWinners = new ArrayList<>();
        List<IMatchPlayer> playerLosers = new ArrayList<>();

        for (IMatchPlayer matchPlayer : this.players.values()) {
            if(computer == null) computer = matchPlayer.gameRank().computor();

            try {
                if(winners.contains(matchPlayer.player().uuid())) playerWinners.add(matchPlayer);
                if(losers.contains(matchPlayer.player().uuid()))  playerLosers.add(matchPlayer);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(this.active())
                try {
                    this.mcLoader.family().parent().connect(matchPlayer.player());
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        if(this.active()) {
            this.mcLoader.dropSession();
            if(unlock) this.mcLoader.unlock();
        }

        // Run storing logic last so that if something happens the other logic ran first.
        try {
            if(computer == null) throw new RuntimeException("Unable to store player ranks for the session: "+this.uuid+"! No computer exists!");

            computer.compute(playerWinners, playerLosers, this.matchmaker, this);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void end(List<UUID> winners, List<UUID> losers) {
        this.end(winners, losers, true);
    }

    public void endTied(boolean unlock) {
        this.ended = true;

        ((Matchmaker) this.matchmaker).remove(this);

        IVelocityPlayerRank.IComputor computer = null;
        for (IMatchPlayer matchPlayer : this.players.values()) {
            if(computer == null) computer = matchPlayer.gameRank().computor();

            if(this.active())
                try {
                    this.mcLoader.family().parent().connect(matchPlayer.player());
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        if(this.active()) {
            this.mcLoader.dropSession();
            if(unlock) this.mcLoader.unlock();
        }

        // Run storing logic last so that if something happens the other logic ran first.
        try {
            if(computer == null) throw new RuntimeException("Unable to store player ranks for the session: "+this.uuid+"! No computer exists!");

            computer.computeTie(this.players.values().stream().toList(), this.matchmaker, this);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void endTied() {
        this.endTied(true);
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("uuid", new JsonPrimitive(this.uuid.toString()));

        JsonArray array = new JsonArray();
        players.values().forEach(matchPlayer -> {
            JsonObject playerObject = new JsonObject();

            playerObject.add("uuid", new JsonPrimitive(matchPlayer.player().uuid().toString()));
            playerObject.add("username", new JsonPrimitive(matchPlayer.player().username()));
            playerObject.add("schema", new JsonPrimitive(matchPlayer.gameRank().schemaName()));
            playerObject.add("rank", matchPlayer.gameRank().toJSON());

            array.add(playerObject);
        });
        object.add("players", array);

        return object;
    }
}
