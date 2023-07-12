package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.*;

public class TPARequest {
    private final Player sender;
    private final Player target;
    private final Date expiration;
    private TPARequestStatus status = TPARequestStatus.NOT_SENT;

    public TPARequest(Player sender, Player target, LiquidTimestamp lifetime) {
        this.sender = sender;
        this.target = target;
        this.expiration = new Date(lifetime.getEpochFromNow());
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

    public boolean expired() {
        if((new Date()).after(this.expiration)) {
            this.status = TPARequestStatus.EXPIRED;
            return true;
        }

        return false;
    }

    public void submit() {
        this.getSender().sendMessage(VelocityLang.TPA_REQUEST_SUBMISSION.build(this.getTarget().getUsername()));
        this.getTarget().sendMessage(VelocityLang.TPA_REQUEST_QUERY.build(this.getSender().getUsername()));
        this.updateStatus(TPARequestStatus.REQUESTED);
    }

    public void deny() {
        this.getSender().sendMessage(VelocityLang.TPA_REQUEST_DENIED_SENDER.build(this.getTarget().getUsername()));
        this.getTarget().sendMessage(VelocityLang.TPA_REQUEST_DENIED_TARGET.build(this.getSender().getUsername()));

        this.updateStatus(TPARequestStatus.DENIED);
    }

    public void accept() {
        VelocityAPI api = VelocityRustyConnector.getAPI();

        DynamicTeleportService dynamicTeleportService = api.getService(DYNAMIC_TELEPORT_SERVICE).orElse(null);
        if(dynamicTeleportService == null) throw new NullPointerException("Dynamic Teleport must be enabled to use tpa functions!");
        TPAService tpaService = dynamicTeleportService.getService(DynamicTeleportService.ValidServices.TPA_SERVICE).orElse(null);
        if(tpaService == null) throw new NullPointerException("TPA in Dynamic Teleport must be enabled to use tpa functions!");

        this.getSender().sendMessage(VelocityLang.TPA_REQUEST_ACCEPTED_SENDER.build(this.getTarget().getUsername()));
        this.getTarget().sendMessage(VelocityLang.TPA_REQUEST_ACCEPTED_TARGET.build(this.getSender().getUsername()));

        try {
            this.updateStatus(TPARequestStatus.ACCEPTED);

            ServerInfo serverInfo = this.getTarget().getCurrentServer().orElseThrow().getServerInfo();
            String familyName = api.getService(SERVER_SERVICE).orElseThrow().findServer(serverInfo).getFamilyName();
            BaseServerFamily family = api.getService(FAMILY_SERVICE).orElseThrow().find(familyName);
            if(family == null) throw new NullPointerException();

            tpaService.tpaSendPlayer(this.getSender(), this.getTarget(), serverInfo);
        } catch (Exception e) {
            this.getSender().sendMessage(VelocityLang.TPA_FAILURE.build(this.getTarget().getUsername()));
            this.getTarget().sendMessage(VelocityLang.TPA_FAILURE_TARGET.build(this.getSender().getUsername()));

            this.updateStatus(TPARequestStatus.STALE);
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