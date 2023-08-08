package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
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

public class FriendsService extends ServiceableService<FriendsServiceHandler> {
    private final Vector<FriendRequest> friendRequests = new Vector<>();
    private final FriendsSettings settings;

    public FriendsService(FriendsSettings settings, FriendsMySQLService friendsMySQLService) {
        super(new FriendsServiceHandler());
        this.services().add(new FriendsDataEnclaveService(friendsMySQLService));
        this.settings = settings;
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

    public List<FriendRequest> findRequestsToTarget(Player target) {
        return this.friendRequests.stream().filter(request -> request.target() == target).findAny().stream().toList();
    }
    public Optional<FriendRequest> findRequest(Player target, Player sender) {
        return this.friendRequests.stream().filter(invite -> invite.target().equals(target) && invite.sender().equals(sender)).findFirst();
    }

    public Optional<List<FakePlayer>> findFriends(Player player, boolean forcePull) {
        List<FakePlayer> friends = new ArrayList<>();
        List<FriendMapping> friendMappings = this.services.dataEnclave().findFriends(player, forcePull).orElse(null);
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

    public FriendMapping sendRequest(Player sender, Player target) {
        if(this.friendCount(sender).orElseThrow() > this.settings().maxFriends())
            sender.sendMessage(Component.text("You have reached the max number of friends!", NamedTextColor.RED));

        FriendRequest friendRequest = new FriendRequest(sender, target);
        this.friendRequests.add(friendRequest);

        sender.sendMessage(Component.text("Friend request sent to " + target.getUsername(), NamedTextColor.GREEN));

        target.sendMessage(VelocityLang.FRIEND_REQUEST.build(sender));
        return new FriendMapping(sender, target);
    }

    public boolean removeFriend(Player sender, Player target) {
        try {
            this.services().dataEnclave().removeFriend(sender, target);
            return true;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception ignore) {}
        return false;
    }

    public void closeInvite(FriendRequest request) {
        this.friendRequests.remove(request);
        request.decompose();
    }

    public Optional<Integer> friendCount(Player player) {
        return this.services().dataEnclave().getFriendCount(player);
    }

    @Override
    public void kill() {
        this.friendRequests.clear();
        super.kill();
    }

    public record FriendsSettings(
            int maxFriends,
            boolean sendNotifications,
            boolean showFamilies,
            boolean allowMessaging
    ) {}
}
