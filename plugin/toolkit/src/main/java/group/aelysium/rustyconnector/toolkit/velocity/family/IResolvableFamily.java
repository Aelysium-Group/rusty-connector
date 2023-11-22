package group.aelysium.rustyconnector.toolkit.velocity.family;

import group.aelysium.rustyconnector.toolkit.velocity.family.bases.IBaseFamily;

import java.util.Optional;

public interface IResolvableFamily {
    String name();
    <TBaseFamily extends IBaseFamily> TBaseFamily resolve();
}
