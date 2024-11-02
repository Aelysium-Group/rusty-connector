package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.crypt.NanoID;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.magic_link.MagicLinkCore;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.plugin.common.lang.CommonLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class CommandRusty {
    private boolean isValid(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) return true;
        reply(sender, RC.Lang("rustyconnector-consoleOnly").generate());
        return false;
    }

    private void reply(CommandSender source, Component response) {
        source.sendMessage(response);
    }
    private void reply(CommandSender source, Error response) {
        source.sendMessage(response.toComponent());
    }


    @Command("rc reload")
    public void nglbwcmuvchdjaon(CommandSender sender) {
        if(!isValid(sender)) return;
        reply(sender, RC.Lang("rustyconnector-moduleReloadList").generate(RC.Kernel().allPlugins().keySet()));
    }

    @Command("rc reload <module>")
    public void nglbwcmuvchdjaon(CommandSender sender, @Argument(value = "module") String module) {
        if(!isValid(sender)) return;
        try {
            if(module.equals("Kernel")) {
                reply(sender, RC.Lang("rustyconnector-waiting").generate());
                Particle.Flux<?> particle = RustyConnector.Toolkit.Proxy().orElseThrow();
                particle.reignite();
                particle.observe();
                reply(sender, RC.Lang("rustyconnector-finished").generate());
                return;
            }

            if(!RC.Kernel().allPlugins().containsKey(module))
                reply(sender, RC.Lang("rustyconnector-moduleReloadList").generate(RC.Kernel().allPlugins().keySet()));

            Particle.Flux<?> particle = RC.Kernel().allPlugins().get(module);
            if(particle == null) throw new NoSuchElementException("No module exists with the name ["+module+"]. Module names are case sensitive.");
            reply(sender, RC.Lang("rustyconnector-waiting").generate());
            particle.reignite();
            particle.observe(1, TimeUnit.MINUTES);
            reply(sender, RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            reply(sender, Error.from(e).toComponent());
        }
    }

    @Command("rc errors")
    public void nglbwzmxvchdjaon(CommandSender sender) {
        if(!isValid(sender)) return;
        RC.Adapter().log(
                Component.join(
                        CommonLang.newlines(),
                        Component.space(),
                        RC.Lang("rustyconnector-border").generate(),
                        Component.space(),
                        RC.Lang().asciiAlphabet().generate("Errors").color(NamedTextColor.BLUE),
                        Component.space(),
                        Component.join(
                                CommonLang.newlines(),
                                RC.Errors().fetchAll().stream().map(e->Component.join(
                                        CommonLang.newlines(),
                                        RC.Lang("rustyconnector-border").generate(),
                                        e.toComponent()
                                )).toList()
                        ),
                        RC.Lang("rustyconnector-border").generate()
                )
        );
    }

    @Command("rc errors <uuid>")
    public void nglbwzmxvchdjaon(CommandSender sender, @Argument(value = "uuid") String uuid) {
        if(!isValid(sender)) return;
        try {
            UUID errorUUID;
            try {
                errorUUID = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                reply(sender, Error.from(e).wrongValue("A valid UUID.", uuid).urgent(true));
                return;
            }

            Error error = RC.Errors().fetch(errorUUID)
                    .orElseThrow(()->new NoSuchElementException("No Error entry exists with the uuid ["+uuid+"]"));
            if(error.throwable() == null) reply(sender, Error.from(new NoSuchElementException("The error ["+uuid+"] doesn't have a throwable to inspect.")));
            RC.Adapter().log(RC.Lang("rustyconnector-exception").generate(error.throwable()));
        } catch (Exception e) {
            reply(sender, Error.from(e).urgent(true));
        }
    }

    @Command("rc message")
    public void yckarhhyoblbmbdl(CommandSender sender) {
        if(!isValid(sender)) return;
        reply(sender, RC.Lang("rustyconnector-messageUsage").generate());
    }

    @Command("rc message list")
    public void pfnommtocuemordk(CommandSender sender) {
        if(!isValid(sender)) return;
        try {
            List<Packet.Remote> messages = RC.S.MagicLink().messageCache().messages();
            reply(sender, RC.Lang("rustyconnector-messagePage").generate(messages, 1, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            reply(sender, Error.from(e).urgent(true));
        }
    }

    @Command("rc message list <page>")
    public void evyfrpitotgxtbmf(CommandSender sender, @Argument("page") int page) {
        if(!isValid(sender)) return;
        try {
            List<Packet.Remote> messages = RC.S.MagicLink().messageCache().messages();
            reply(sender, RC.Lang("rustyconnector-messagePage").generate(messages, page, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            reply(sender, Error.from(e).urgent(true));
        }
    }

    @Command("rc message get")
    public void scfjnwbsynzbksyh(CommandSender sender) {
        if(!isValid(sender)) return;
        reply(sender, RC.Lang("rustyconnector-messageGetUsage").generate());
    }

    @Command("rc message get <id>")
    public void nidbtmkngikxlzyo(CommandSender sender, @Argument("id") String id) {
        if(!isValid(sender)) return;
        try {
            reply(sender, RC.Lang("rustyconnector-message").generate(RC.S.MagicLink().messageCache().findMessage(NanoID.fromString(id))));
        } catch (Exception e) {
            reply(sender, Error.from(e).urgent(true));
        }
    }

    @Command("send")
    public void acmednrmiufxxviz(CommandSender sender) {
        if(!isValid(sender)) return;
        reply(sender, RC.Lang("rustyconnector-sendUsage").generate());
    }
    @Command("send <username>")
    public void acmednrmiusgxviz(CommandSender sender, @Argument("username") String username) {
        acmednrmiufxxviz(sender);
    }

    @Command("send <username> <family_name>")
    private  void sertgsdbfdfxxviz(CommandSender sender, @Argument("username") String username, @Argument("family_name") String family_name) {
        if(!isValid(sender)) return;
        try {
            UUID uuid = RC.S.Adapter().playerUUID(username)
                    .orElseThrow(()->new NoSuchElementException("Unable to get the uuid for the username ["+username+"]."));

            Packet.Local packet = RC.S.Kernel().send(uuid, family_name);

            packet.onReply(p -> {
                MagicLinkCore.Packets.ResponsePacket response = new MagicLinkCore.Packets.ResponsePacket(p);
                RC.S.Adapter().log(Component.text(response.message()));
            });
        } catch (Exception e) {
            reply(sender, Error.from(e).urgent(true));
        }
    }
    @Command("send <username> server <server_uuid>")
    private  void sertgsdbgrfxxviz(CommandSender sender, @Argument("username") String username, @Argument("server_uuid") String server_uuid) {
        if(!isValid(sender)) return;
        try {
            UUID uuid = RC.S.Adapter().playerUUID(username)
                    .orElseThrow(()->new NoSuchElementException("Unable to get the uuid for the username ["+username+"]."));

            UUID server = UUID.fromString(server_uuid);

            Packet.Local packet = RC.S.Kernel().send(uuid, server);

            packet.onReply(p -> {
                MagicLinkCore.Packets.ResponsePacket response = new MagicLinkCore.Packets.ResponsePacket(p);
                RC.S.Adapter().log(Component.text(response.message()));
            });
        } catch (Exception e) {
            reply(sender, Error.from(e).urgent(true));
        }
    }

    @Command("lock")
    private void zxcvssdbfdfxxviz(CommandSender sender) {
        if(!isValid(sender)) return;
        RC.S.Kernel().lock();
    }

    @Command("unlock")
    private void sertgssdfvsscefd(CommandSender sender) {
        if(!isValid(sender)) return;
        RC.S.Kernel().unlock();
    }
}