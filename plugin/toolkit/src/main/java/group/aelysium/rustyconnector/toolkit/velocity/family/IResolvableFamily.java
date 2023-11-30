package group.aelysium.rustyconnector.toolkit.velocity.family;

public interface IResolvableFamily {
    String name();
    <TBaseFamily extends IFamily> TBaseFamily resolve();
}
