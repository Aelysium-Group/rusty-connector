package group.aelysium.rustyconnector.api.core.packet;

public interface PacketHandler<TPacket extends IPacket> {
    void execute(TPacket packet) throws Exception;
}
