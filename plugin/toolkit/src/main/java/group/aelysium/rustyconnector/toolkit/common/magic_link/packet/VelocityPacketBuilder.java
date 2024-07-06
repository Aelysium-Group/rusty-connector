package group.aelysium.rustyconnector.toolkit.common.magic_link.packet;

import group.aelysium.rustyconnector.toolkit.proxy.central.ICoreServiceHandler;

public class VelocityPacketBuilder implements Service {
    private final VelocityFlame<? extends ICoreServiceHandler> flame;

    public VelocityPacketBuilder(VelocityFlame<? extends ICoreServiceHandler> flame) {
        this.flame = flame;
    }

    public Packet.ProxyPacketBuilder newBuilder() {
        return new Packet.ProxyPacketBuilder(flame);
    }

    @Override
    public void kill() {}
}
