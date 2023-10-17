package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.hash.Snowflake;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands.CommandFM;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands.CommandFriends;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands.CommandUnFriend;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class FriendsService extends Service {
    private final Cache<Long, FriendRequest> friendRequests;
    private final FriendsSettings settings;
    private final Snowflake snowflakeGenerator = new Snowflake();
    private final FriendsDataEnclave dataEnclave;

    public FriendsService(FriendsSettings settings) throws Exception {
        this.settings = settings;

        this.friendRequests = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        this.dataEnclave = new FriendsDataEnclave(this.settings.storage());
    }

    public void initCommand() {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        Tinder.get().logger().send(Component.text("Building friends service commands...", NamedTextColor.DARK_GRAY));

        if(!commandManager.hasCommand("friends"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("friends").build(),
                        CommandFriends.create(this)
                );

                Tinder.get().logger().send(Component.text(" | Registered: /friends", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }
        if(!commandManager.hasCommand("unfriend"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("unfriend").build(),
                        CommandUnFriend.create(this)
                );

                Tinder.get().logger().send(Component.text(" | Registered: /unfriend", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }
        if(this.settings.allowMessaging())
            if(!commandManager.hasCommand("fm"))
                try {
                    commandManager.register(
                            commandManager.metaBuilder("fm").build(),
                            CommandFM.create(this)
                    );

                    Tinder.get().logger().send(Component.text(" | Registered: /fm", NamedTextColor.YELLOW));
                } catch (Exception e) {
                    e.printStackTrace();
                }

        Tinder.get().logger().send(Component.text("Finished building friends service commands.", NamedTextColor.GREEN));
    }

    public FriendsSettings settings() {
        return this.settings;
    }

    public List<FriendRequest> findRequestsToTarget(FakePlayer target) {
        List<Map.Entry<Long, FriendRequest>> entries = this.friendRequests.asMap().entrySet().stream().filter(request -> request.getValue().target().equals(target)).findAny().stream().toList();

        List<FriendRequest> requests = new ArrayList<>();
        for (Map.Entry<Long, FriendRequest> entry : entries)
            requests.add(entry.getValue());

        return requests;
    }
    public Optional<FriendRequest> findRequest(FakePlayer target, FakePlayer sender) {
        Optional<Map.Entry<Long, FriendRequest>> entry = this.friendRequests.asMap().entrySet().stream().filter(invite -> invite.getValue().target().equals(target) && invite.getValue().sender().equals(sender)).findFirst();
        return entry.map(Map.Entry::getValue);
    }

    public Optional<List<FakePlayer>> findFriends(Player player) {
        List<FakePlayer> friends = new ArrayList<>();
        List<FriendMapping> friendMappings = this.dataEnclave.findFriends(FakePlayer.from(player)).orElse(null);
        if(friendMappings == null) return Optional.empty();

        friendMappings.forEach(mapping -> {
            try {
                friends.add(mapping.fetchOther(FakePlayer.from(player)));
            } catch (NullPointerException ignore) {}
        });

        return Optional.of(friends);
    }

    public boolean areFriends(FakePlayer player1, FakePlayer player2) {
        return this.dataEnclave.areFriends(player1, player2);
    }
    public void addFriends(FakePlayer player1, FakePlayer player2) {
        this.dataEnclave.addFriend(player1, player2);
    }
    public void removeFriends(FakePlayer player1, FakePlayer player2) {
        this.dataEnclave.removeFriend(player1, player2);
    }

    public FriendMapping sendRequest(Player sender, FakePlayer target) {
        if(this.friendCount(FakePlayer.from(sender)).orElseThrow() > this.settings().maxFriends())
            sender.sendMessage(VelocityLang.MAX_FRIENDS_REACHED);

        FakePlayer fakeSender = FakePlayer.from(sender);
        FriendRequest friendRequest = new FriendRequest(snowflakeGenerator.nextId(), fakeSender, target);
        this.friendRequests.put(friendRequest.id(), friendRequest);


        try {
            target.resolve().orElseThrow().sendMessage(VelocityLang.FRIEND_REQUEST.build(sender));
            sender.sendMessage(VelocityLang.FRIEND_REQUEST_SENT.build(target.username()));
        } catch (NoSuchElementException ignore) {
            sender.sendMessage(VelocityLang.FRIEND_REQUEST_TARGET_NOT_ONLINE.build(target.username()));
        }

        return FriendMapping.from(fakeSender, target);
    }

    public void closeInvite(FriendRequest request) {
        this.friendRequests.invalidate(request.id());
        request.decompose();
    }

    public Optional<Long> friendCount(FakePlayer player) {
        return this.dataEnclave.getFriendCount(player);
    }

    @Override
    public void kill() {
        this.friendRequests.asMap().forEach((key, value) -> value.decompose());
        this.friendRequests.invalidateAll();

        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        commandManager.unregister("friends");
        commandManager.unregister("unfriend");
        commandManager.unregister("fm");
    }

    public record FriendsSettings(
            MySQLStorage storage,
            int maxFriends,
            boolean sendNotifications,
            boolean showFamilies,
            boolean allowMessaging
    ) {}
}
