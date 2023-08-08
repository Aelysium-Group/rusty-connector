package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.util.Date;

public class TPARequest {
    private final Player sender;
    private final Player target;
    private final Date expiration;
    private TPARequestStatus status = TPARequestStatus.NOT_SENT;

    public TPARequest(Player sender, Player target, LiquidTimestamp lifetime) {
        this.sender = sender;
        this.target = target;
        this.expiration = new Date(lifetime.epochFromNow());
    }

    private void updateStatus(TPARequestStatus status) {
        this.status = status;
    }

    public Player sender() {
        return sender;
    }

    public Player target() {
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
        this.sender().sendMessage(VelocityLang.TPA_REQUEST_SUBMISSION.build(this.target().getUsername()));
        this.target().sendMessage(VelocityLang.TPA_REQUEST_QUERY.build(this.sender()));
        this.updateStatus(TPARequestStatus.REQUESTED);
    }

    public void ignore() {
        this.sender().sendMessage(VelocityLang.TPA_REQUEST_DENIED_SENDER.build(this.target().getUsername()));
        this.target().sendMessage(VelocityLang.TPA_REQUEST_DENIED_TARGET.build(this.sender().getUsername()));

        this.updateStatus(TPARequestStatus.DENIED);
    }

    public void accept() {
        VelocityAPI api = VelocityAPI.get();

        DynamicTeleportService dynamicTeleportService = api.services().dynamicTeleportService().orElse(null);
        if(dynamicTeleportService == null) throw new NullPointerException("Dynamic Teleport must be enabled to use tpa functions!");
        TPAService tpaService = dynamicTeleportService.services().tpaService().orElse(null);
        if(tpaService == null) throw new NullPointerException("TPA in Dynamic Teleport must be enabled to use tpa functions!");

        try {
            this.updateStatus(TPARequestStatus.ACCEPTED);

            ServerInfo serverInfo = this.target().getCurrentServer().orElseThrow().getServerInfo();
            PlayerServer server = api.services().serverService().search(serverInfo);
            BaseServerFamily family = server.family();
            if(family == null) throw new NullPointerException();

            tpaService.tpaSendPlayer(this.sender(), this.target(), server);

            this.sender().sendMessage(VelocityLang.TPA_REQUEST_ACCEPTED_SENDER.build(this.target().getUsername()));
            this.target().sendMessage(VelocityLang.TPA_REQUEST_ACCEPTED_TARGET.build(this.sender().getUsername()));
        } catch (Exception e) {
            e.printStackTrace();
            this.sender().sendMessage(VelocityLang.TPA_FAILURE.build(this.target().getUsername()));
            this.target().sendMessage(VelocityLang.TPA_FAILURE_TARGET.build(this.sender().getUsername()));

            this.updateStatus(TPARequestStatus.STALE);
        }
    }

    @Override
    public String toString() {
        return "<TPARequest Sender=" + this.sender().getUsername() +" "+
               "Target="+ this.target().getUsername() +" "+
               "Status="+ this.status +" "+
               "Expiration="+ this.expiration.toString() +" "+
               ">";
    }
}