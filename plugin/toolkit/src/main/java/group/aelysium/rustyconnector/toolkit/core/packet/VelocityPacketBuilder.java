package group.aelysium.rustyconnector.toolkit.core.packet;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityFlame;

public class VelocityPacketBuilder implements Service {
    private final VelocityFlame<? extends ICoreServiceHandler> flame;

    public VelocityPacketBuilder(VelocityFlame<? extends ICoreServiceHandler> flame) {
        this.flame = flame;
    }

    public GenericPacket.ProxyPacketBuilder startNew() {
        return new GenericPacket.ProxyPacketBuilder(flame);
    }

    @Override
    public void kill() {}
}
