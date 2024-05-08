package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPARequest;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;

import java.util.Date;

public class TPARequest implements ITPARequest {
    private final IPlayer sender;
    private final IPlayer target;
    private final Date expiration;
    private TPARequestStatus status = TPARequestStatus.NOT_SENT;

    public TPARequest(IPlayer sender, IPlayer target, LiquidTimestamp lifetime) {
        this.sender = sender;
        this.target = target;
        this.expiration = new Date(lifetime.epochFromNow());
    }

    private void updateStatus(TPARequestStatus status) {
        this.status = status;
    }

    public IPlayer sender() {
        return sender;
    }

    public IPlayer target() {
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
        this.sender().sendMessage(ProxyLang.TPA_REQUEST_SUBMISSION.build(this.target().username()));
        this.target().sendMessage(ProxyLang.TPA_REQUEST_QUERY.build((group.aelysium.rustyconnector.plugin.velocity.lib.players.Player) this.sender()));
        this.updateStatus(TPARequestStatus.REQUESTED);
    }

    public void deny() {
        this.sender().sendMessage(ProxyLang.TPA_REQUEST_DENIED_SENDER.build(this.target().username()));
        this.target().sendMessage(ProxyLang.TPA_REQUEST_DENIED_TARGET.build(this.sender().username()));

        this.updateStatus(TPARequestStatus.DENIED);
    }

    public void accept() {
        Tinder api = Tinder.get();

        DynamicTeleportService dynamicTeleportService = api.services().dynamicTeleport().orElseThrow(() -> new NullPointerException("Dynamic Teleport must be enabled to use tpa functions!"));
        TPAService tpaService = dynamicTeleportService.services().tpa().orElseThrow(() -> new NullPointerException("TPA in Dynamic Teleport must be enabled to use tpa functions!"));

        try {
            this.updateStatus(TPARequestStatus.ACCEPTED);

            MCLoader server = (MCLoader) this.target().server().orElseThrow();

            tpaService.tpaSendPlayer(this.sender(), this.target(), server);

            this.sender().sendMessage(ProxyLang.TPA_REQUEST_ACCEPTED_SENDER.build(this.target().username()));
            this.target().sendMessage(ProxyLang.TPA_REQUEST_ACCEPTED_TARGET.build(this.sender().username()));
        } catch (Exception e) {
            e.printStackTrace();
            this.sender().sendMessage(ProxyLang.TPA_FAILURE.build(this.target().username()));
            this.target().sendMessage(ProxyLang.TPA_FAILURE_TARGET.build(this.sender().username()));

            this.updateStatus(TPARequestStatus.STALE);
        }
    }

    @Override
    public String toString() {
        return "<TPARequest Sender=" + this.sender().username() +" "+
               "Target="+ this.target().username() +" "+
               "Status="+ this.status +" "+
               "Expiration="+ this.expiration.toString() +" "+
               ">";
    }
}