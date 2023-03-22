package group.aelysium.rustyconnector.plugin.velocity.lib.tpa;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;

import java.util.NoSuchElementException;

public class TPARequest {
    private final Player sender;
    private final Player target;
    private TPARequestStatus status = TPARequestStatus.NOT_SENT;

    public TPARequest(Player sender, Player target) {
        this.sender = sender;
        this.target = target;
    }

    private void updateStatus(TPARequestStatus status) {
        this.status = status;
    }

    public Player getSender() {
        return sender;
    }

    public Player getTarget() {
        return target;
    }

    public void submit() {
        this.updateStatus(TPARequestStatus.REQUESTED);
    }

    public void deny() {
        this.getSender().sendMessage(VelocityLang.TPA_REQUEST_DENIED_SENDER.build(this.getTarget().getUsername()));
        this.getTarget().sendMessage(VelocityLang.TPA_REQUEST_DENIED_TARGET.build(this.getSender().getUsername()));

        this.updateStatus(TPARequestStatus.DENIED);
    }

    public void accept() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        this.getSender().sendMessage(VelocityLang.TPA_REQUEST_ACCEPTED_SENDER.build(this.getTarget().getUsername()));
        this.getTarget().sendMessage(VelocityLang.TPA_REQUEST_ACCEPTED_TARGET.build(this.getSender().getUsername()));

        try {
            this.updateStatus(TPARequestStatus.ACCEPTED);

            ServerInfo serverInfo = this.getTarget().getCurrentServer().orElseThrow().getServerInfo();
            String familyName = plugin.getProxy().findServer(serverInfo).getFamilyName();
            ServerFamily<? extends PaperServerLoadBalancer> family = plugin.getProxy().getFamilyManager().find(familyName);
            if(family == null) throw new NullPointerException();

            plugin.getProxy().tpaSendPlayer(this.getSender(), this.getTarget(), serverInfo);
        } catch (Exception e) {
            this.getSender().sendMessage(VelocityLang.TPA_FAILURE.build(this.getTarget().getUsername()));
            this.getTarget().sendMessage(VelocityLang.TPA_FAILURE_TARGET.build(this.getSender().getUsername()));

            this.updateStatus(TPARequestStatus.STALE);
        }
    }
}