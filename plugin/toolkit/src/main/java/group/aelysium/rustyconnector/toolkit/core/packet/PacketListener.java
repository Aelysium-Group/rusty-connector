package group.aelysium.rustyconnector.toolkit.core.packet;

public interface PacketListener {
    /**
     * The identifier that will be used to decide if this listener should
     * be executed for the passed packet.
     * @return {@link PacketType.Mapping}
     */
    PacketType.Mapping identifier();

    <TPacket extends IPacket> void execute(TPacket packet) throws Exception;
}
