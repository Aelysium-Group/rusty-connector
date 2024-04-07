package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.reactors.StorageReactor;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IStaticFamily;
import group.aelysium.rustyconnector.toolkit.velocity.friends.PlayerPair;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IDatabase;

import java.util.*;

public class Database extends StorageReactor.Holder implements IDatabase, Service {
    private final Players players;
    private final FriendLinks friends;
    private final ServerResidences residences;
    private final PlayerRanks ranks;

    public Database(StorageReactor reactor) {
        super(reactor);
        this.players = new Players(reactor);
        this.friends = new FriendLinks(reactor);
        this.residences = new ServerResidences(reactor);
        this.ranks = new PlayerRanks(reactor);
    }

    public Players players() {
        return this.players;
    }
    public FriendLinks friends() {
        return this.friends;
    }
    public ServerResidences residences() {
        return this.residences;
    }
    public PlayerRanks ranks() {
        return this.ranks;
    }

    public void kill() {
        this.reactor.kill();
    }

    public static class Players extends StorageReactor.Holder implements IDatabase.Players {
        public Players(StorageReactor reactor) {
            super(reactor);
        }

        public void set(IPlayer player) {
            this.reactor.savePlayer(player.uuid(), player.username());
        }

        public Optional<IPlayer> get(UUID uuid) {
            return this.reactor.fetchPlayer(uuid);
        }
        public Optional<IPlayer> get(String username) {
            return this.reactor.fetchPlayer(username);
        }
    }
    public static class FriendLinks extends StorageReactor.Holder implements IDatabase.FriendLinks {
        public FriendLinks(StorageReactor reactor) {
            super(reactor);
        }

        public void set(IPlayer player1, IPlayer player2) {
            this.reactor.saveFriendLink(PlayerPair.from(player1, player2));
        }

        public Optional<List<IPlayer>> get(IPlayer player) {
            return this.reactor.fetchFriends(player.uuid());
        }

        public void delete(IPlayer player1, IPlayer player2) {
            this.reactor.deleteFriendLink(PlayerPair.from(player1, player2));
        }

        public Optional<Boolean> contains(IPlayer player1, IPlayer player2) {
            return this.reactor.areFriends(PlayerPair.from(player1, player2));
        }
    }
    public static class ServerResidences extends StorageReactor.Holder implements IDatabase.ServerResidences {
        public ServerResidences(StorageReactor reactor) {
            super(reactor);
        }

        public void set(IStaticFamily family, IMCLoader mcLoader, IPlayer player) {
            this.reactor.saveServerResidence(family.id(), mcLoader.uuid(), player.uuid(), family.homeServerExpiration().epochFromNow());
        }

        public void delete(String familyId) {
            this.reactor.deleteServerResidences(familyId);
        }

        public void delete(String familyId, IPlayer player) {
            this.reactor.deleteServerResidence(familyId, player.uuid());
        }

        public Optional<IServerResidence.MCLoaderEntry> get(IStaticFamily family, IPlayer player) {
            return this.reactor.fetchServerResidence(family.id(), player.uuid());
        }

        public void purgeExpired() {
            this.reactor.purgeExpiredServerResidences();
        }

        public void refreshExpirations(IStaticFamily family) {
            if(family.homeServerExpiration() == null)
                this.reactor.updateExpirations(family.id(), family.homeServerExpiration().epochFromNow());
            else
                this.reactor.updateExpirations(family.id(), null);
        }
    }
    public static class PlayerRanks extends StorageReactor.Holder implements IDatabase.PlayerRanks {
        public PlayerRanks(StorageReactor reactor) {
            super(reactor);
        }

        public void deleteGame(String gameId) {
            this.reactor.deleteGame(gameId);
        }

        public void delete(IPlayer player) {
            this.reactor.deleteRank(player.uuid());
        }

        public void delete(IPlayer player, String gameId) {
            this.reactor.deleteRank(player.uuid(), gameId);
        }

        public void set(IMatchPlayer<IPlayerRank> player) {
            if(player instanceof RandomizedPlayerRank) return; // Storing randomized player ranks is a literal waste of space.

            this.reactor.saveRank(player.player().uuid(), player.gameId(), player.rankSchemaName(), player.rankToJSON());
        }

        public Optional<IPlayerRank> get(IPlayer player, String gameId) {
            return this.reactor.fetchRank(player.uuid(), gameId);
        }
    }
}