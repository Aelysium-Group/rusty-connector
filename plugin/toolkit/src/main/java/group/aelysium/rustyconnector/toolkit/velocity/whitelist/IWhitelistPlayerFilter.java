package group.aelysium.rustyconnector.toolkit.velocity.whitelist;

import java.util.UUID;

public interface IWhitelistPlayerFilter {
    UUID uuid();
    String username();
    String ip();
}