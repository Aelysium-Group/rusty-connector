package group.aelysium.rustyconnector.toolkit.common.magic_link.packet;

import org.jetbrains.annotations.NotNull;

public abstract class PacketListener<Packet extends IPacket> {
    private final PacketIdentification target;
    private final Wrapper<Packet> wrapper;

    public PacketListener(@NotNull PacketIdentification target, @NotNull Wrapper<Packet> wrapper) {
        this.target = target;
        this.wrapper = wrapper;
    }

    protected abstract void execute(Packet packet) throws Exception;

    /**
     * The target will be used to decide if this listener should be executed for the passed packet.
     * @return {@link PacketIdentification}
     */
    public final PacketIdentification target() {
        return this.target;
    }

    public final void wrapAndExecute(IPacket packet) throws Exception {
        this.execute(this.wrapper.wrap(packet));
    }

    /**
     * Used to wrap the packet as the correct packet for the handler to use.
     * @param <Packet>
     */
    public static abstract class Wrapper<Packet extends IPacket> {
        public abstract Packet wrap(IPacket packet);
    }
}
