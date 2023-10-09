package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model;

import java.util.List;

public record User(String username, char[] password, List<Role> roles) {
}
