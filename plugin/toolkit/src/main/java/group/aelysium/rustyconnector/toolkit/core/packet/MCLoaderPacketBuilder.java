package group.aelysium.rustyconnector.toolkit.core.packet;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderFlame;

public class MCLoaderPacketBuilder implements Service {
    private final IMCLoaderFlame<? extends ICoreServiceHandler> flame;

    public MCLoaderPacketBuilder(IMCLoaderFlame<? extends ICoreServiceHandler> flame) {
        this.flame = flame;
    }

    public GenericPacket.MCLoaderPacketBuilder startNew() {
        return new GenericPacket.MCLoaderPacketBuilder(flame);
    }

    @Override
    public void kill() {}
}
