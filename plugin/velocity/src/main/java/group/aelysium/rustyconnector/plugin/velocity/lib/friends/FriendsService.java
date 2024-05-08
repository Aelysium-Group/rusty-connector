package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.friends.FriendsServiceSettings;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendRequest;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendsService;
import group.aelysium.rustyconnector.toolkit.velocity.friends.PlayerPair;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IDatabase;
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
    private final Cache<UUID, IFriendRequest> friendRequests;
    private final FriendsServiceSettings settings;

    public FriendsService(FriendsServiceSettings settings) throws Exception {
        this.settings = settings;

        this.friendRequests = CacheBuilder.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
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
        List<Map.Entry<UUID, IFriendRequest>> entries = this.friendRequests.asMap().entrySet().stream().filter(request -> request.getValue().target().equals(target.username())).findAny().stream().toList();

        List<IFriendRequest> requests = new ArrayList<>();
        for (Map.Entry<UUID, IFriendRequest> entry : entries)
            requests.add(entry.getValue());

        return requests;
    }

    public IDatabase.FriendLinks friendStorage() {
        return this.settings.storage().database().friends();
    }

    public Optional<IFriendRequest> findRequest(IPlayer target, IPlayer sender) {
        Optional<Map.Entry<UUID, IFriendRequest>> entry = this.friendRequests.asMap().entrySet().stream().filter(invite ->
                invite.getValue().target().equals(target.username()) && invite.getValue().sender().equals(sender)
        ).findFirst();
        return entry.map(Map.Entry::getValue);
    }

    public void sendRequest(IPlayer sender, String targetUsername) {
        if(this.friendCount(sender) > this.settings().maxFriends())
            sender.sendMessage(ProxyLang.MAX_FRIENDS_REACHED);

        IFriendRequest friendRequest = new FriendRequest(this, sender, targetUsername);
        this.friendRequests.put(friendRequest.uuid(), friendRequest);

        try {
            Player player = new IPlayer.UsernameReference(targetUsername).get();
            if(!player.online()) throw new NoOutputException();
            player.sendMessage(ProxyLang.FRIEND_REQUEST.build(sender));
            sender.sendMessage(ProxyLang.FRIEND_REQUEST_SENT.build(targetUsername));
        } catch (Exception ignore) {
            sender.sendMessage(ProxyLang.FRIEND_REQUEST_TARGET_NOT_ONLINE.build(targetUsername));
        }
    }

    public void closeInvite(IFriendRequest request) {
        this.friendRequests.invalidate(request.uuid());
        request.decompose();
    }

    public long friendCount(IPlayer player) {
        return this.settings.storage().database().friends().get(player).orElseGet(ArrayList::new).size();
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
