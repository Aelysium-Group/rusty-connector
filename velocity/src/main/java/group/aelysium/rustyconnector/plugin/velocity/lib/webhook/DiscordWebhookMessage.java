package group.aelysium.rustyconnector.plugin.velocity.lib.webhook;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

/**
 * Thank you to <a href="https://github.com/LuckPerms/LuckPerms">...</a> for inspiring this implementation.
 */
public interface DiscordWebhookMessage {
    String IMAGE_PROXY__REGISTER_ALL = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480087037591653/PROXY__REGISTER_ALL.png";
    String IMAGE_PROXY__SERVER_REGISTER = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480087289237537/PROXY__SERVER_REGISTER.png";
    String IMAGE_PROXY__SERVER_UNREGISTER = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480087515734099/PROXY__SERVER_UNREGISTER.png";
    String IMAGE_PROXY__PLAYER_JOIN = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480141227982858/PROXY__PLAYER_JOIN.png";
    String IMAGE_PROXY__PLAYER_LEAVE = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480086454571078/PROXY__PLAYER_LEAVE.png";
    String IMAGE_PROXY__DISCONNECT_CATCH = "https://cdn.discordapp.com/attachments/1098811303679774851/1100493810779303946/FAMILY__DISCONNECT_CATCH.png";
    String IMAGE_FAMILY__REGISTER_ALL = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480045790797994/FAMILY__REGISTER_ALL.png";
    String IMAGE_FAMILY__SERVER_REGISTER = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480046013100092/FAMILY__SERVER_REGISTER.png";
    String IMAGE_FAMILY__SERVER_UNREGISTER = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480046235390033/FAMILY__SERVER_UNREGISTER.png";
    String IMAGE_FAMILY__PLAYER_JOIN = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480045111320686/FAMILY__PLAYER_JOIN.png";
    String IMAGE_FAMILY__PLAYER_LEAVE = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480045325234400/FAMILY__PLAYER_LEAVE.png";

    String IMAGE__GENERIC_SWITCH = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480045539135508/FAMILY__PLAYER_SWITCH.png";
    String IMAGE__PLAYER_FAMILY_SWITCH = "https://cdn.discordapp.com/attachments/1098811303679774851/1100480086794313748/PROXY__PLAYER_SWITCH.png";
    String IMAGE__PLAYER_SERVER_SWITCH = "https://cdn.discordapp.com/attachments/1098811303679774851/1100481467492077740/SERVER__PLAYER_SWITCH.png";

    Integer COLOR_AQUA  = 0x36F5F3;
    Integer COLOR_GREEN = 0x5DD672;
    Integer COLOR_RED   = 0xEB4F38;
    Integer COLOR_BLUE  = 0x7D67F5;
    Integer COLOR_PINK  = 0xFF50B8;

    WebhookEmbed PROXY__REGISTER_ALL =
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Register All Servers",
                                    IMAGE_PROXY__REGISTER_ALL, null
                            )
                    )
                    .setColor(COLOR_AQUA)
                    .build();

    ParameterizedEmbed2<PlayerServer, String> PROXY__SERVER_REGISTER = (server, familyName) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                "Server: " + server.getServerInfo().getName(),
                                IMAGE_PROXY__SERVER_REGISTER, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Was registered into: " + familyName, null)
                    )
                    .setColor(COLOR_GREEN)
                    .build();

    ParameterizedEmbed1<PlayerServer> PROXY__SERVER_UNREGISTER = (server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Server: " + server.getServerInfo().getName(),
                                    IMAGE_PROXY__SERVER_UNREGISTER, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Was unregistered from: " + server.getFamilyName(), null)
                    )
                    .setColor(COLOR_RED)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> PROXY__PLAYER_JOIN = (player, server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Joined the network",
                                    IMAGE_PROXY__PLAYER_JOIN, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle(player.getUsername(), null)
                    )
                    .addField(new WebhookEmbed.EmbedField(true, "Family", server.getFamilyName()))
                    .addField(new WebhookEmbed.EmbedField(true, "Server", server.getServerInfo().getName()))
                    .setColor(COLOR_GREEN)
                    .build();

    ParameterizedEmbed1<Player> PROXY__PLAYER_LEAVE = (player) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Left the network",
                                    IMAGE_PROXY__PLAYER_LEAVE, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle(player.getUsername(), null)
                    )
                    .setColor(COLOR_RED)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> PROXY__PLAYER_JOIN_FAMILY = (player, server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    IMAGE_FAMILY__PLAYER_JOIN, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Joined the family: " + server.getFamilyName(),null)
                    )
                    .addField(new WebhookEmbed.EmbedField(false, "Server", server.getServerInfo().getName()))
                    .setColor(COLOR_GREEN)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> PROXY__PLAYER_LEAVE_FAMILY = (player, server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    IMAGE_FAMILY__PLAYER_LEAVE, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Left the family: " + server.getFamilyName(), null)
                    )
                    .setColor(COLOR_RED)
                    .build();

    ParameterizedEmbed3<Player, PlayerServer, PlayerServer> PROXY__PLAYER_SWITCH_SERVER = (player, oldServer, newServer) -> {
        if(oldServer.getFamilyName().equals(newServer.getFamilyName()))
            return new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    IMAGE__PLAYER_SERVER_SWITCH, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Switched servers from " + oldServer.getServerInfo().getName() + " to " + newServer.getServerInfo().getName(), null)
                    )
                    .setColor(COLOR_PINK)
                    .build();


        return new WebhookEmbedBuilder()
                .setAuthor(
                        new WebhookEmbed.EmbedAuthor(
                                player.getUsername(),
                                IMAGE__PLAYER_SERVER_SWITCH, null
                        )
                )
                .setTitle(
                        new WebhookEmbed.EmbedTitle("Switched servers from " + oldServer.getServerInfo().getName() + " to " + newServer.getServerInfo().getName(), null)
                )
                .addField(new WebhookEmbed.EmbedField(true, "Old Family", oldServer.getServerInfo().getName()))
                .addField(new WebhookEmbed.EmbedField(true, "New Family", newServer.getServerInfo().getName()))
                .setColor(COLOR_PINK)
                .build();
    };

    ParameterizedEmbed3<Player, PlayerServer, PlayerServer> PROXY__PLAYER_SWITCH_FAMILY = (player, oldServer, newServer) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    IMAGE__PLAYER_FAMILY_SWITCH, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Switched families from " + oldServer.getFamilyName() + " to " + newServer.getFamilyName(), null)
                    )
                    .setColor(COLOR_BLUE)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> PROXY__DISCONNECT_CATCH = (player, newServer) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    IMAGE_PROXY__DISCONNECT_CATCH, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Was caught by " + newServer.getFamilyName() + " after being disconnected from another server", null)
                    )
                    .setColor(COLOR_BLUE)
                    .build();


    ParameterizedEmbed1<String> FAMILY__REGISTER_ALL = (familyName) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Family: " + familyName,
                                    IMAGE_FAMILY__REGISTER_ALL, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Requested to register all of its servers",null)
                    )
                    .setColor(COLOR_AQUA)
                    .build();
    ParameterizedEmbed2<PlayerServer, String> FAMILY__SERVER_REGISTER = (server, familyName) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Registered",
                                    IMAGE_FAMILY__SERVER_REGISTER, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle(server.getServerInfo().getName(), null)
                    )
                    .addField(new WebhookEmbed.EmbedField(false, "Family", familyName))
                    .setColor(COLOR_GREEN)
                    .build();
    ParameterizedEmbed1<PlayerServer> FAMILY__SERVER_UNREGISTER = (server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Unregistered",
                                    IMAGE_FAMILY__SERVER_UNREGISTER, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle(server.getServerInfo().getName(), null)
                    )
                    .addField(new WebhookEmbed.EmbedField(false, "Family", server.getFamilyName()))
                    .setColor(COLOR_RED)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> FAMILY__PLAYER_JOIN = (player, server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    IMAGE_FAMILY__PLAYER_JOIN, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Joined the family: ", null)
                    )
                    .addField(new WebhookEmbed.EmbedField(true, "Family", server.getFamilyName()))
                    .addField(new WebhookEmbed.EmbedField(true, "Server", server.getServerInfo().getName()))
                    .setColor(COLOR_GREEN)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> FAMILY__PLAYER_LEAVE = (player, server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    IMAGE_FAMILY__PLAYER_LEAVE, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Left the family: ", null)
                    )
                    .addField(new WebhookEmbed.EmbedField(true, "Family", server.getFamilyName()))
                    .addField(new WebhookEmbed.EmbedField(true, "Server", server.getServerInfo().getName()))
                    .setColor(COLOR_RED)
                    .build();

    ParameterizedEmbed3<Player, PlayerServer, PlayerServer> FAMILY__PLAYER_SWITCH = (player, oldServer, newServer) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    IMAGE__PLAYER_SERVER_SWITCH, null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Switched servers from " + oldServer.getServerInfo().getName() + " to " + newServer.getServerInfo().getName(), null)
                    )
                    .addField(new WebhookEmbed.EmbedField(false, "Family", newServer.getFamilyName()))
                    .setColor(COLOR_PINK)
                    .build();

    interface ParameterizedEmbed1<A1> {
        WebhookEmbed build(A1 arg1);
    }
    interface ParameterizedEmbed2<A1, A2> {
        WebhookEmbed build(A1 arg1, A2 arg2);
    }
    interface ParameterizedEmbed3<A1, A2, A3> {
        WebhookEmbed build(A1 arg1, A2 arg2, A3 arg3);
    }
    interface ParameterizedEmbed4<A1, A2, A3, A4> {
        WebhookEmbed build(A1 arg1, A2 arg2, A3 arg3, A4 arg4);
    }
}




