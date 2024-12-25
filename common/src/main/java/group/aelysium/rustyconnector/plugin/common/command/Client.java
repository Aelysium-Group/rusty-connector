package group.aelysium.rustyconnector.plugin.common.command;

import group.aelysium.rustyconnector.common.errors.Error;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract class Client {
    protected final Object source;

    public Client(@NotNull Object source) {
        this.source = source;
    }

    public abstract void send(Component message);
    public abstract void send(Error error);
    public <S> S toSender() {
        return (S) this.source;
    }

    public static abstract class Player<P> extends Client {
        public Player(@NotNull P source) {
            super(source);
        }
    }
    public static abstract class Console<C> extends Client {
        public Console(@NotNull C source) {
            super(source);
        }
    }
    public static abstract class Other<O> extends Client {
        public Other(@NotNull O source) {
            super(source);
        }
    }
}
