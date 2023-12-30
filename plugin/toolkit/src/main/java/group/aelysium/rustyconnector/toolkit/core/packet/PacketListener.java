package group.aelysium.rustyconnector.toolkit.core.packet;

public abstract class PacketListener<TPacket extends GenericPacket> {
    /**
     * The target will be used to decide if this listener should be executed for the passed packet.
     * @return {@link PacketIdentification}
     */
    public abstract PacketIdentification target();

    public abstract void execute(TPacket packet) throws Exception;

    /**
     * Used by RC internals.
     */
    public void genericExecute(GenericPacket packet) throws Exception {
        this.execute((TPacket) packet);
    }
}
