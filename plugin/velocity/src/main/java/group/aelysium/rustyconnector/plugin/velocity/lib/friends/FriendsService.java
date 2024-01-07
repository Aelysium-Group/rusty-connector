package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.toolkit.velocity.friends.FriendsServiceSettings;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendRequest;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendsService;
import group.aelysium.rustyconnector.core.lib.crypt.Snowflake;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands.CommandFM;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands.CommandFriends;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands.CommandUnFriend;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class FriendsService implements IFriendsService {
    private final Cache<Long, IFriendRequest> friendRequests;
    private final FriendsServiceSettings settings;
    private final Snowflake snowflakeGenerator = new Snowflake();
    private final FriendsDataEnclave dataEnclave;

    public FriendsService(FriendsServiceSettings settings) throws Exception {
        this.settings = settings;

        this.friendRequests = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        this.dataEnclave = new FriendsDataEnclave(this.settings.storage());
    }

    public void initCommand(DependencyInjector.DI1<List<Component>> dependencies) {
        List<Component> bootOutput = dependencies.d1();
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        bootOutput.add(Component.text("Building friends service commands...", NamedTextColor.DARK_GRAY));

        if(!commandManager.hasCommand("friends"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("friends").build(),
                        CommandFriends.create(this)
                );

                bootOutput.add(Component.text(" | Registered: /friends", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }
        if(!commandManager.hasCommand("unfriend"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("unfriend").build(),
                        CommandUnFriend.create(this)
                );

                bootOutput.add(Component.text(" | Registered: /unfriend", NamedTextColor.YELLOW));
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

                    bootOutput.add(Component.text(" | Registered: /fm", NamedTextColor.YELLOW));
                } catch (Exception e) {
                    e.printStackTrace();
                }

        bootOutput.add(Component.text("Finished building friends service commands.", NamedTextColor.GREEN));
    }

    public FriendsServiceSettings settings() {
        return this.settings;
    }

    public List<IFriendRequest> findRequestsToTarget(IPlayer target) {
        List<Map.Entry<Long, IFriendRequest>> entries = this.friendRequests.asMap().entrySet().stream().filter(request -> request.getValue().target().equals(target)).findAny().stream().toList();

        List<IFriendRequest> requests = new ArrayList<>();
        for (Map.Entry<Long, IFriendRequest> entry : entries)
            requests.add(entry.getValue());

        return requests;
    }
    public Optional<IFriendRequest> findRequest(IPlayer target, IPlayer sender) {
        Optional<Map.Entry<Long, IFriendRequest>> entry = this.friendRequests.asMap().entrySet().stream().filter(invite -> invite.getValue().target().equals(target) && invite.getValue().sender().equals(sender)).findFirst();
        return entry.map(Map.Entry::getValue);
    }

    public Optional<List<IPlayer>> findFriends(IPlayer player) {
        List<IPlayer> friends = new ArrayList<>();
        List<FriendMapping> friendMappings = this.dataEnclave.findFriends(player).orElse(null);
        if(friendMappings == null) return Optional.empty();

        friendMappings.forEach(mapping -> {
            try {
                friends.add(mapping.fetchOther(player));
            } catch (NullPointerException ignore) {}
        });

        return Optional.of(friends);
    }

    public boolean areFriends(IPlayer player1, IPlayer player2) {
        return this.dataEnclave.areFriends(player1, player2);
    }
    public void addFriends(IPlayer player1, IPlayer player2) {
        this.dataEnclave.addFriend(player1, player2);
    }
    public void removeFriends(IPlayer player1, IPlayer player2) {
        this.dataEnclave.removeFriend(player1, player2);
    }

    public FriendMapping sendRequest(IPlayer sender, IPlayer target) {
        if(this.friendCount(sender).orElseThrow() > this.settings().maxFriends())
            sender.sendMessage(ProxyLang.MAX_FRIENDS_REACHED);

        IFriendRequest friendRequest = new FriendRequest(this, snowflakeGenerator.nextId(), sender, target);
        this.friendRequests.put(friendRequest.id(), friendRequest);


        try {
            target.resolve().orElseThrow().sendMessage(ProxyLang.FRIEND_REQUEST.build(sender));
            sender.sendMessage(ProxyLang.FRIEND_REQUEST_SENT.build(target.username()));
        } catch (NoSuchElementException ignore) {
            sender.sendMessage(ProxyLang.FRIEND_REQUEST_TARGET_NOT_ONLINE.build(target.username()));
        }

        return FriendMapping.from(sender, target);
    }

    public void closeInvite(IFriendRequest request) {
        this.friendRequests.invalidate(request.id());
        request.decompose();
    }

    public Optional<Long> friendCount(IPlayer player) {
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
}
