package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface;


import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.mc_loader.ranked_game_interface.IRankedGameInterfaceService;

import java.util.UUID;

public class RankedGameInterfaceService implements IRankedGameInterfaceService {
    private UUID uuid = null;

    @Override
    public void associateGame(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void endGame() {
        MCLoaderTinder api = TinderAdapterForCore.getTinder();
        api.services().packetBuilder().endRankedGame(this.uuid);
    }

    public void kill() {}
}
