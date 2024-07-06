package group.aelysium.rustyconnector.toolkit.proxy.family;

import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;
import group.aelysium.rustyconnector.toolkit.proxy.family.whitelist.Whitelist;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public abstract class Family implements IPlayer.Connectable, MCLoader.Factory, Particle {
    protected final String id;
    protected final String displayName;
    protected final String parent;
    protected final Flux<Whitelist> whitelist;

    protected Family(
            @NotNull String id,
            @Nullable String displayName,
            @Nullable String parent,
            @Nullable Flux<Whitelist> whitelist
    ) {
        this.id = id;
        this.displayName = displayName;
        this.parent = parent;
        this.whitelist = whitelist;
    }

    public @NotNull String id() {
        return this.id;
    }
    public @Nullable String displayName() {
        return this.displayName;
    }

    public abstract long players();

    /**
     * Fetches a reference to the parent of this family.
     * The parent of this family should always be either another family, or the root family.
     * If this family is the root family, this method will always return `null`.
     */
    public @NotNull Optional<Flux<Family>> parent() {
        if(this.parent == null) return Optional.empty();
        try {
            return RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().Families().orElseThrow().find(this.parent);
        } catch (Exception ignore) {}
        return Optional.empty();
    }

    /**
     * Gets the whitelist flux used for this connector.
     * If there's no whitelist for this family connector, this will return an empty optional.
     */
    public @NotNull Optional<Flux<Whitelist>> whitelist() {
        return Optional.ofNullable(this.whitelist);
    }

    /**
     * Returns the details of this family in a component which can be
     * printed to the console or sent to a player.
     */
    public abstract @NotNull Component details();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Family that = (Family) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public void close() throws Exception {
        try {
            if(this.whitelist == null) throw new NullPointerException();
            this.whitelist.close();
        } catch (Exception ignore) {}
    }
}
