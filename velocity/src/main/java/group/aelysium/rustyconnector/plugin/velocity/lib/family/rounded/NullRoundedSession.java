package group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded;

public class NullRoundedSession extends RoundedSession {
    private static final NullRoundedSession session = new NullRoundedSession(null);

    public NullRoundedSession(RoundedSession group) {
        super(group);
    }

    public static NullRoundedSession get() {
        return session;
    }
}
