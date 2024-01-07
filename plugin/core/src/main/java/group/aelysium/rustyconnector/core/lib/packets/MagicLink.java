package group.aelysium.rustyconnector.core.lib.packets;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;
import group.aelysium.rustyconnector.toolkit.core.server.ServerAssignment;
import group.aelysium.rustyconnector.toolkit.velocity.util.ColorMapper;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public interface MagicLink {
    interface Handshake {
        class Ping extends GenericPacket {
            public String address() {
                return this.parameters.get(Parameters.ADDRESS).getAsString();
            }
            public Optional<String> displayName() {
                String displayName = this.parameters.get(Parameters.DISPLAY_NAME).getAsString();
                if(displayName.isEmpty()) return Optional.empty();
                return Optional.of(displayName);
            }
            public String magicConfigName() {
                return this.parameters.get(Parameters.MAGIC_CONFIG_NAME).getAsString();
            }
            public Integer playerCount() {
                return this.parameters.get(Parameters.PLAYER_COUNT).getAsInt();
            }
            public String podName() {
                return this.parameters.get(Parameters.POD_NAME).getAsString();
            }

            public Ping(Integer messageVersion, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
                super(messageVersion, PacketIdentification.Predefined.MAGICLINK_HANDSHAKE, sender, target, parameters);
            }

            public interface Parameters {
                String ADDRESS = "a";
                String DISPLAY_NAME = "n";
                String MAGIC_CONFIG_NAME = "c";
                String PLAYER_COUNT = "pc";
                String POD_NAME = "k8";
            }
        }

        class Failure extends GenericPacket {
            public String reason() {
                return this.parameters.get(Parameters.REASON).getAsString();
            }

            public Failure(Integer messageVersion, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
                super(messageVersion, PacketIdentification.Predefined.MAGICLINK_HANDSHAKE_FAIL, sender, target, parameters);
            }

            public interface Parameters {
                String REASON = "r";

                static List<String> toList() {
                    List<String> list = new ArrayList<>();
                    list.add(REASON);

                    return list;
                }
            }
        }

        class Success extends GenericPacket {
            public String message() {
                return this.parameters.get(Parameters.MESSAGE).getAsString();
            }
            public NamedTextColor color() {
                return ColorMapper.map(this.parameters.get(Parameters.COLOR).getAsString());
            }
            public Integer pingInterval() {
                return this.parameters.get(Parameters.INTERVAL).getAsInt();
            }
            public ServerAssignment assignment() {
                try {
                    return ServerAssignment.valueOf(this.parameters.get(Parameters.ASSIGNMENT).getAsString());
                } catch (Exception ignore) {}
                return ServerAssignment.GENERIC;
            }

            public Success(Integer messageVersion, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
                super(messageVersion, PacketIdentification.Predefined.MAGICLINK_HANDSHAKE_SUCCESS, sender, target, parameters);
            }

            public interface Parameters {
                String MESSAGE = "m";
                String COLOR = "c";
                String INTERVAL = "i";
                String ASSIGNMENT = "a";

                static List<String> toList() {
                    List<String> list = new ArrayList<>();
                    list.add(MESSAGE);
                    list.add(COLOR);
                    list.add(ASSIGNMENT);
                    list.add(INTERVAL);

                    return list;
                }
            }
        }
    }

    class Disconnect extends GenericPacket {
        public Disconnect(Integer messageVersion, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
            super(messageVersion, PacketIdentification.Predefined.MAGICLINK_HANDSHAKE_KILL, sender, target, parameters);
        }
    }
}
