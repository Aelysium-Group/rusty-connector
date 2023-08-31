package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.hash.Snowflake;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands.CommandFM;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands.CommandFriends;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands.CommandUnFriend;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class FriendsService extends ServiceableService<FriendsServiceHandler> {
    private final Cache<Long, FriendRequest> friendRequests;
    private final FriendsSettings settings;
    private final Snowflake snowflakeGenerator = new Snowflake();

    public FriendsService(FriendsSettings settings, FriendsMySQLService friendsMySQLService) {
        super(new FriendsServiceHandler());
        this.services().add(new FriendsDataEnclaveService(friendsMySQLService));
        this.settings = settings;

        this.friendRequests = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    public void initCommand() {
        CommandManager commandManager = VelocityAPI.get().velocityServer().getCommandManager();
        VelocityAPI.get().logger().send(Component.text("Building friends service commands...", NamedTextColor.DARK_GRAY));

        if(!commandManager.hasCommand("friends"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("friends").build(),
                        CommandFriends.create()
                );

                VelocityAPI.get().logger().send(Component.text(" | Registered: /friends", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }
        if(!commandManager.hasCommand("unfriend"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("unfriend").build(),
                        CommandUnFriend.create()
                );

                VelocityAPI.get().logger().send(Component.text(" | Registered: /unfriend", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }
        if(this.settings.allowMessaging())
            if(!commandManager.hasCommand("fm"))
                try {
                    commandManager.register(
                            commandManager.metaBuilder("fm").build(),
                            CommandFM.create()
                    );

                    VelocityAPI.get().logger().send(Component.text(" | Registered: /fm", NamedTextColor.YELLOW));
                } catch (Exception e) {
                    e.printStackTrace();
                }

        VelocityAPI.get().logger().send(Component.text("Finished building friends service commands.", NamedTextColor.GREEN));
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

    public Optional<List<FakePlayer>> findFriends(Player player, boolean forcePull) {
        List<FakePlayer> friends = new ArrayList<>();
        List<FriendMapping> friendMappings = this.services.dataEnclave().findFriends(FakePlayer.from(player), forcePull).orElse(null);
        if(friendMappings == null) return Optional.empty();

        friendMappings.forEach(mapping -> {
            try {
                friends.add(mapping.friendOf(player));
            } catch (NullPointerException ignore) {}
        });

        return Optional.of(friends);
    }

    public boolean areFriends(Player player1, Player player2) {
        return this.services.dataEnclave().areFriends(FakePlayer.from(player1), FakePlayer.from(player2));
    }

    public boolean areFriends(FakePlayer player1, FakePlayer player2) {
        return this.services.dataEnclave().areFriends(player1, player2);
    }

    public FriendMapping sendRequest(Player sender, FakePlayer target) {
        if(this.friendCount(FakePlayer.from(sender)).orElseThrow() > this.settings().maxFriends())
            sender.sendMessage(Component.text("You have reached the max number of friends!", NamedTextColor.RED));

        FakePlayer fakeSender = FakePlayer.from(sender);
        FriendRequest friendRequest = new FriendRequest(snowflakeGenerator.nextId(), fakeSender, target);
        this.friendRequests.put(friendRequest.id(), friendRequest);


        try {
            target.resolve().orElseThrow().sendMessage(VelocityLang.FRIEND_REQUEST.build(sender));
            sender.sendMessage(Component.text("Friend request sent to " + target.username() + "! It will expire in 10 minutes.", NamedTextColor.GREEN));
        } catch (NoSuchElementException ignore) {
            sender.sendMessage(Component.text(target.username() + " doesn't seem to be online, we'll let them know about your friend request when they log in! Your request will expire in 10 minutes.", NamedTextColor.GREEN));
        }

        return new FriendMapping(fakeSender, target);
    }

    public boolean removeFriend(FakePlayer sender, FakePlayer target) {
        try {
            this.services().dataEnclave().removeFriend(sender, target);
            return true;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception ignore) {}
        return false;
    }

    public void closeInvite(FriendRequest request) {
        this.friendRequests.invalidate(request.id());
        request.decompose();
    }

    public Optional<Integer> friendCount(FakePlayer player) {
        return this.services().dataEnclave().getFriendCount(player);
    }

    @Override
    public void kill() {
        this.friendRequests.asMap().forEach((key, value) -> value.decompose());
        this.friendRequests.invalidateAll();
        super.kill();

        CommandManager commandManager = VelocityAPI.get().velocityServer().getCommandManager();
        commandManager.unregister("friends");
        commandManager.unregister("unfriend");
        commandManager.unregister("fm");
    }

    public record FriendsSettings(
            int maxFriends,
            boolean sendNotifications,
            boolean showFamilies,
            boolean allowMessaging
    ) {}
}
