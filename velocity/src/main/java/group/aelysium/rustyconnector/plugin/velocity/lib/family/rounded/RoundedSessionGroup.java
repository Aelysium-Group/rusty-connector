package group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded;

import com.velocitypowered.api.proxy.Player;

import java.util.Vector;
import java.util.function.Consumer;

public class RoundedSessionGroup {
    private final Vector<Player> players = new Vector<>();
    private final int minPlayers;
    private final int maxPlayers;

    public RoundedSessionGroup(int minPlayers, int maxPlayers) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getPlayerCount() {
        return this.players.size();
    }


    public boolean add(Player player) {
        if(this.players.contains(player)) return true;
        return this.players.add(player);
    }

    public boolean remove(Player player) {
        return this.players.remove(player);
    }

    public boolean contains(Player player) {
        return this.players.contains(player);
    }

    public void forEach(Consumer<? super Player> action) {
        this.players.forEach(action);
    }
}
