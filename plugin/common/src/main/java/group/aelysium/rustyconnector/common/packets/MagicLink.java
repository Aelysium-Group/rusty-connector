package group.aelysium.rustyconnector.common.packets;

import group.aelysium.rustyconnector.toolkit.common.packet.Packet;
import group.aelysium.rustyconnector.toolkit.velocity.util.ColorMapper;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public interface MagicLink {
    interface Handshake {
        class Ping extends Packet.Wrapper {
            public String address() {
                return this.parameters().get(Parameters.ADDRESS).getAsString();
            }
            public Optional<String> displayName() {
                String displayName = this.parameters().get(Parameters.DISPLAY_NAME).getAsString();
                if(displayName.isEmpty()) return Optional.empty();
                return Optional.of(displayName);
            }
            public String magicConfigName() {
                return this.parameters().get(Parameters.MAGIC_CONFIG_NAME).getAsString();
            }
            public Integer playerCount() {
                return this.parameters().get(Parameters.PLAYER_COUNT).getAsInt();
            }
            public Optional<String> podName() {
                String podName = this.parameters().get(Parameters.POD_NAME).getAsString();
                if(podName.isEmpty()) return Optional.empty();
                return Optional.of(podName);
            }

            public Ping(Packet packet) {
                super(packet);
            }

            public interface Parameters {
                String ADDRESS = "a";
                String DISPLAY_NAME = "n";
                String MAGIC_CONFIG_NAME = "c";
                String PLAYER_COUNT = "pc";
                String POD_NAME = "pn";
            }
        }

        class Failure extends Packet.Wrapper {
            public String reason() {
                return this.parameters().get(Parameters.REASON).getAsString();
            }

            public Failure(Packet packet) {
                super(packet);
            }

            public interface Parameters {
                String REASON = "r";
            }
        }

        class Success extends Packet.Wrapper {
            public String message() {
                return this.parameters().get(Parameters.MESSAGE).getAsString();
            }
            public NamedTextColor color() {
                return ColorMapper.map(this.parameters().get(Parameters.COLOR).getAsString());
            }
            public Integer pingInterval() {
                return this.parameters().get(Parameters.INTERVAL).getAsInt();
            }

            public Success(Packet packet) {
                super(packet);
            }

            public interface Parameters {
                String MESSAGE = "m";
                String COLOR = "c";
                String INTERVAL = "i";
            }
        }
    }

    class Disconnect extends Packet.Wrapper {
        public Disconnect(Packet packet) {
            super(packet);
        }
    }

    class StalePing extends Packet.Wrapper {
        public StalePing(Packet packet) {
            super(packet);
        }
    }
}