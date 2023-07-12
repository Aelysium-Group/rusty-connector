package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.*;

public class DynamicTeleport_TPARequest {
    private final Player sender;
    private final Player target;
    private final Date expiration;
    private DynamicTeleport_TPARequestStatus status = DynamicTeleport_TPARequestStatus.NOT_SENT;

    public DynamicTeleport_TPARequest(Player sender, Player target, int lifetime) {
        this.sender = sender;
        this.target = target;
        this.expiration = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(lifetime));
    }

    private void updateStatus(DynamicTeleport_TPARequestStatus status) {
        this.status = status;
    }

    public Player getSender() {
        return sender;
    }

    public Player getTarget() {
        return target;
    }

    public boolean expired() {
        if((new Date()).after(this.expiration)) {
            this.status = DynamicTeleport_TPARequestStatus.EXPIRED;
            return true;
        }

        return false;
    }

    public void submit() {
        this.getSender().sendMessage(VelocityLang.TPA_REQUEST_SUBMISSION.build(this.getTarget().getUsername()));
        this.getTarget().sendMessage(VelocityLang.TPA_REQUEST_QUERY.build(this.getSender().getUsername()));
        this.updateStatus(DynamicTeleport_TPARequestStatus.REQUESTED);
    }

    public void deny() {
        this.getSender().sendMessage(VelocityLang.TPA_REQUEST_DENIED_SENDER.build(this.getTarget().getUsername()));
        this.getTarget().sendMessage(VelocityLang.TPA_REQUEST_DENIED_TARGET.build(this.getSender().getUsername()));

        this.updateStatus(DynamicTeleport_TPARequestStatus.DENIED);
    }

    public void accept() {
        VelocityAPI api = VelocityRustyConnector.getAPI();

        this.getSender().sendMessage(VelocityLang.TPA_REQUEST_ACCEPTED_SENDER.build(this.getTarget().getUsername()));
        this.getTarget().sendMessage(VelocityLang.TPA_REQUEST_ACCEPTED_TARGET.build(this.getSender().getUsername()));

        try {
            this.updateStatus(DynamicTeleport_TPARequestStatus.ACCEPTED);

            ServerInfo serverInfo = this.getTarget().getCurrentServer().orElseThrow().getServerInfo();
            String familyName = api.getService(SERVER_SERVICE).orElseThrow().findServer(serverInfo).getFamilyName();
            BaseServerFamily family = api.getService(FAMILY_SERVICE).orElseThrow().find(familyName);
            if(family == null) throw new NullPointerException();

            api.getService(DYNAMIC_TELEPORT_TPA_CLEANING_SERVICE).orElseThrow().tpaSendPlayer(this.getSender(), this.getTarget(), serverInfo);
        } catch (Exception e) {
            this.getSender().sendMessage(VelocityLang.TPA_FAILURE.build(this.getTarget().getUsername()));
            this.getTarget().sendMessage(VelocityLang.TPA_FAILURE_TARGET.build(this.getSender().getUsername()));

            this.updateStatus(DynamicTeleport_TPARequestStatus.STALE);
        }
    }

    @Override
    public String toString() {
        return "<TPARequest Sender=" + this.getSender().getUsername() +" "+
               "Target="+ this.getTarget().getUsername() +" "+
               "Status="+ this.status +" "+
               "Expiration="+ this.expiration.toString() +" "+
               ">";
    }
}