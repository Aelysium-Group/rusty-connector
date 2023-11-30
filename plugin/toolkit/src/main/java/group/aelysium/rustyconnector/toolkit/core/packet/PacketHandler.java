package group.aelysium.rustyconnector.toolkit.core.packet;

public interface PacketHandler {
    <TPacket extends IPacket> void execute(TPacket packet) throws Exception;
}
