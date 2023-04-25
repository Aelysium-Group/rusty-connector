package group.aelysium.rustyconnector.plugin.velocity.lib.webhook;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

/**
 * Thank you to <a href="https://github.com/LuckPerms/LuckPerms">...</a> for inspiring this implementation.
 */
public interface DiscordWebhookMessage {

    WebhookEmbed PROXY__REGISTER_ALL =
            new WebhookEmbedBuilder()
                    .setColor(0x6684504)
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Register All Servers",
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1098813040025157652/icons8-all-128.png", null
                            )
                    )
                    .build();

    ParameterizedEmbed2<PlayerServer, String> PROXY__SERVER_REGISTER = (server, familyName) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                "Server: " + server.getServerInfo().getName(),
                                "https://cdn.discordapp.com/attachments/1098811303679774851/1098811341415923742/icons8-upload-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Was registered into: " + familyName, null)
                    )
                    .setColor(0x02CA90)
                    .build();

    ParameterizedEmbed1<PlayerServer> PROXY__SERVER_UNREGISTER = (server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Server: " + server.getServerInfo().getName(),
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1098811341139095562/icons8-blocked-128.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Was unregistered from " + server.getFamilyName(), null)
                    )
                    .setColor(0xCC3913)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> PROXY__PLAYER_JOIN = (player, server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Joined the network",
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1100113134330580992/icons8-add-user-male-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle(player.getUsername(), null)
                    )
                    .addField(new WebhookEmbed.EmbedField(true, "Family", server.getFamilyName()))
                    .addField(new WebhookEmbed.EmbedField(true, "Server", server.getServerInfo().getName()))
                    .setColor(0x0CB5B1)
                    .build();

    ParameterizedEmbed1<Player> PROXY__PLAYER_LEAVE = (player) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Left the network",
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1100113134330580992/icons8-add-user-male-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle(player.getUsername(), null)
                    )
                    .setColor(0xCA2525)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> PROXY__PLAYER_JOIN_FAMILY = (player, server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1100113134330580992/icons8-add-user-male-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Joined the family: " + server.getFamilyName(),null)
                    )
                    .addField(new WebhookEmbed.EmbedField(false, "Server", server.getServerInfo().getName()))
                    .setColor(0x02CA90)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> PROXY__PLAYER_LEAVE_FAMILY = (player, server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1100113134330580992/icons8-add-user-male-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Left the family: " + server.getFamilyName(), null)
                    )
                    .setColor(0xCA2525)
                    .build();

    ParameterizedEmbed3<Player, PlayerServer, PlayerServer> PROXY__PLAYER_SWITCH_SERVER = (player, oldServer, newServer) -> {
        if(oldServer.getFamilyName().equals(newServer.getFamilyName()))
            return new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1100113134330580992/icons8-add-user-male-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Switched servers from " + oldServer.getServerInfo().getName() + " to " + newServer.getServerInfo().getName(), null)
                    )
                    .setColor(0x0CB5B1)
                    .build();


        return new WebhookEmbedBuilder()
                .setAuthor(
                        new WebhookEmbed.EmbedAuthor(
                                player.getUsername(),
                                "https://cdn.discordapp.com/attachments/1098811303679774851/1100113134330580992/icons8-add-user-male-96.png", null
                        )
                )
                .setTitle(
                        new WebhookEmbed.EmbedTitle("Switched servers from " + oldServer.getServerInfo().getName() + " to " + newServer.getServerInfo().getName(), null)
                )
                .addField(new WebhookEmbed.EmbedField(true, "Old Family", oldServer.getServerInfo().getName()))
                .addField(new WebhookEmbed.EmbedField(true, "New Family", newServer.getServerInfo().getName()))
                .setColor(0x0CB5B1)
                .build();
    };

    ParameterizedEmbed3<Player, PlayerServer, PlayerServer> PROXY__PLAYER_SWITCH_FAMILY = (player, oldServer, newServer) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1100113134330580992/icons8-add-user-male-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Switched families from " + oldServer.getFamilyName() + " to " + newServer.getFamilyName(), null)
                    )
                    .setColor(0x0CB5B1)
                    .build();


    ParameterizedEmbed1<String> FAMILY__REGISTER_ALL = (familyName) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    familyName,
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1098813040025157652/icons8-all-128.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Requested to register all of its servers",null)
                    )
                    .setColor(0x6684504)
                    .build();
    ParameterizedEmbed2<PlayerServer, String> FAMILY__SERVER_REGISTER = (server, familyName) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Registered",
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1098811341415923742/icons8-upload-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle(server.getServerInfo().getName(), null)
                    )
                    .addField(new WebhookEmbed.EmbedField(false, "Family", familyName))
                    .setColor(0x02CA90)
                    .build();
    ParameterizedEmbed1<PlayerServer> FAMILY__SERVER_UNREGISTER = (server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    "Unregistered",
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1098811341139095562/icons8-blocked-128.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle(server.getServerInfo().getName(), null)
                    )
                    .addField(new WebhookEmbed.EmbedField(false, "Family", server.getFamilyName()))
                    .setColor(0xCC3913)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> FAMILY__PLAYER_JOIN = (player, server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1100113134330580992/icons8-add-user-male-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Joined the family: ", null)
                    )
                    .addField(new WebhookEmbed.EmbedField(true, "Family", server.getFamilyName()))
                    .addField(new WebhookEmbed.EmbedField(true, "Server", server.getServerInfo().getName()))
                    .setColor(0x0F8F1F)
                    .build();

    ParameterizedEmbed2<Player, PlayerServer> FAMILY__PLAYER_LEAVE = (player, server) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1100113134330580992/icons8-add-user-male-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Left the family: ", null)
                    )
                    .addField(new WebhookEmbed.EmbedField(true, "Family", server.getFamilyName()))
                    .addField(new WebhookEmbed.EmbedField(true, "Server", server.getServerInfo().getName()))
                    .setColor(0xCA2525)
                    .build();

    ParameterizedEmbed3<Player, PlayerServer, PlayerServer> FAMILY__PLAYER_SWITCH = (player, oldServer, newServer) ->
            new WebhookEmbedBuilder()
                    .setAuthor(
                            new WebhookEmbed.EmbedAuthor(
                                    player.getUsername(),
                                    "https://cdn.discordapp.com/attachments/1098811303679774851/1100113134330580992/icons8-add-user-male-96.png", null
                            )
                    )
                    .setTitle(
                            new WebhookEmbed.EmbedTitle("Switched servers from " + oldServer.getServerInfo().getName() + " to " + newServer.getServerInfo().getName(), null)
                    )
                    .addField(new WebhookEmbed.EmbedField(false, "Family", newServer.getFamilyName()))
                    .setColor(0x0CB5B1)
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




