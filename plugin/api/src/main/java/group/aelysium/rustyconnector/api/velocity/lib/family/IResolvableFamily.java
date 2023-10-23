package group.aelysium.rustyconnector.api.velocity.lib.family;

import group.aelysium.rustyconnector.api.velocity.lib.family.bases.IBaseFamily;

import java.util.Optional;

public interface IResolvableFamily {
    String name();
    Optional<? extends IBaseFamily> resolve();
}
