package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.model.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.config.FriendsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;

public class FriendsService extends ServiceableService {
    private final Vector<FriendRequest> friendRequests = new Vector<>();
    private final FriendsSettings settings;

    private FriendsService(FriendsMySQLService friendsMySQLService, FriendsSettings settings) {
        super(new HashMap<>());
        this.services.put(FriendsDataEnclaveService.class, new FriendsDataEnclaveService(friendsMySQLService));
        this.settings = settings;
    }

    public FriendsSettings getSettings() {
        return this.settings;
    }

    public List<FriendRequest> findRequestsToTarget(Player target) {
        return this.friendRequests.stream().filter(request -> request.getTarget() == target).findAny().stream().toList();
    }
    public Optional<FriendRequest> findRequest(Player target, Player sender) {
        return this.friendRequests.stream().filter(invite -> invite.getTarget().equals(target) && invite.getSender().equals(sender)).findFirst();
    }

    public Optional<List<FriendMapping>> findFriends(Player player) {
        return this.getService(ValidServices.DATA_ENCLAVE).orElseThrow().findFriends(player);
    }

    public FriendMapping sendRequest(Player sender, Player target) {
        if(this.getFriendCount(sender).orElseThrow() > this.getSettings().maxFriends())
            sender.sendMessage(Component.text("You have reached the max number of friends!", NamedTextColor.RED));

        FriendRequest friendRequest = new FriendRequest(sender, target);
        this.friendRequests.add(friendRequest);

        sender.sendMessage(Component.text("Friend request sent to " + target.getUsername(), NamedTextColor.GREEN));

        target.sendMessage(Component.text("Hey! "+ sender.getUsername() +" wants to be your friend!", NamedTextColor.GRAY));
        target.sendMessage(Component.text("Accept their friend request: ", NamedTextColor.GRAY).append(Component.text("/friend requests "+sender.getUsername()+" accept", NamedTextColor.GREEN)));
        target.sendMessage(Component.text("Deny their friend request: ", NamedTextColor.GRAY).append(Component.text("/friend requests "+sender.getUsername()+" deny", NamedTextColor.RED)));
        return new FriendMapping(sender, target);
    }

    public boolean removeFriend(Player sender, Player target) {
        try {
            this.getService(ValidServices.DATA_ENCLAVE).orElseThrow().removeFriend(sender, target);
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

    public Optional<Integer> getFriendCount(Player player) {
        return this.getService(ValidServices.DATA_ENCLAVE).orElseThrow().getFriendCount(player);
    }

    @Override
    public void kill() {

    }

    public static FriendsService init(FriendsConfig config) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, SQLException {

        FriendsMySQLService mySQLService = new FriendsMySQLService.Builder()
                .setHost(config.getMysql_host())
                .setPort(config.getMysql_port())
                .setDatabase(config.getMysql_database())
                .setUser(config.getMysql_user())
                .setPassword(config.getMysql_password())
                .build();

        return new FriendsService(mySQLService, new FriendsSettings(config.getMaxFriends()));
    }

    /**
     * The services that are valid for this service provider.
     */
    public static class ValidServices {
        public static Class<FriendsDataEnclaveService> DATA_ENCLAVE = FriendsDataEnclaveService.class;
    }

    public record FriendsSettings(
            int maxFriends
    ) {}
}
