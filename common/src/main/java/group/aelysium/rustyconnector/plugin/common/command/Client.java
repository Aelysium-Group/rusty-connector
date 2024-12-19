package group.aelysium.rustyconnector.plugin.common.command;

import group.aelysium.rustyconnector.common.errors.Error;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract class Client<S> {
    protected final S source;

    public Client(@NotNull S source) {
        this.source = source;
    }

    public abstract void send(Component message);
    public abstract void send(Error error);
    public S toSender() {
        return this.source;
    }

    public static abstract class Player<P> extends Client<P> {
        public Player(@NotNull P source) {
            super(source);
        }
    }
    public static abstract class Console<C> extends Client<C> {
        public Console(@NotNull C source) {
            super(source);
        }
    }
    public static abstract class Other<O> extends Client<O> {
        public Other(@NotNull O source) {
            super(source);
        }
    }
}
