package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.RankedGame;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game.RankedGameReadyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RankedGameReadyListener extends PacketListener<RankedGame.Ready> {
    protected IMCLoaderTinder api;

    public RankedGameReadyListener(IMCLoaderTinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return BuiltInIdentifications.RANKED_GAME_READY;
    }

    @Override
    public RankedGame.Ready wrap(Packet packet) {
        return new RankedGame.Ready(packet);
    }

    @Override
    public void execute(RankedGame.Ready packet) {
        JsonObject object = new Gson().fromJson(packet.parameters().get("session").getAsString(), JsonObject.class);

        UUID uuid = UUID.fromString(object.get("uuid").getAsString());

        List<UUID> players = new ArrayList<>();
        JsonArray array = object.get("players").getAsJsonArray();
        array.forEach(item -> players.add(UUID.fromString(item.getAsString())));

        TinderAdapterForCore.getTinder().services().rankedGameInterface().orElseThrow().session(uuid, players);
        TinderAdapterForCore.getTinder().services().events().fire(new RankedGameReadyEvent(uuid, players));
    }
}
