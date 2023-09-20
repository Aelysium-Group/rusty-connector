package group.aelysium.rustyconnector.core.lib.packets;

public abstract class PacketHandler {
    public abstract void execute(GenericPacket genericPacket) throws Exception;
}
