package group.aelysium.rustyconnector.toolkit.mc_loader.ranked_game_interface;

import com.google.gson.JsonObject;

import java.util.UUID;

public record MCLoaderMatchPlayer(UUID uuid, String username, String schema, JsonObject rank) {}
