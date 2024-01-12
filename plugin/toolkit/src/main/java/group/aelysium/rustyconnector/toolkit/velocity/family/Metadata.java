package group.aelysium.rustyconnector.toolkit.velocity.family;

public record Metadata(
        StateType stateType,
        boolean canBeAParentFamily,
        boolean tpaAllowed,
        boolean supportsInjectors
        ) {
    public enum StateType {
        STATEFUL,
        STATELESS
    }


    public static Metadata STATIC_FAMILY_META = new Metadata(
            Metadata.StateType.STATEFUL,
            true,
            true,
            true
    );
    public static Metadata SCALAR_FAMILY_META = new Metadata(
            Metadata.StateType.STATELESS,
            true,
            true,
            true
    );
    public static Metadata ROOT_FAMILY_META = new Metadata(
            Metadata.StateType.STATELESS,
            true,
            true,
            true
    );
    public static Metadata RANKED_FAMILY_META = new Metadata(
            StateType.STATELESS,
            false,
            false,
            false
    );
}
