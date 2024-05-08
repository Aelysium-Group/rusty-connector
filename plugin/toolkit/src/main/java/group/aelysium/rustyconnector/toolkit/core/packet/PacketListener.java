package group.aelysium.rustyconnector.toolkit.core.packet;

public abstract class PacketListener<TPacketWrapper extends Packet.Wrapper> {
    /**
     * The target will be used to decide if this listener should be executed for the passed packet.
     * @return {@link PacketIdentification}
     */
    public abstract PacketIdentification target();

    /**
     * Wraps the packet so that it can be passed to {@link #execute(Packet.Wrapper)}.
     * @param packet The packet to wrap.
     */
    public abstract TPacketWrapper wrap(Packet packet);

    public abstract void execute(TPacketWrapper packet) throws Exception;

    public void wrapAndExecute(Packet packet) throws Exception {
        this.execute(this.wrap(packet));
    }
}
