package group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded;

public class NullRoundedSession extends RoundedSession {
    private static final NullRoundedSession session = new NullRoundedSession();

    private NullRoundedSession() {
        super(0, 0);
    }

    public static NullRoundedSession get() {
        return session;
    }
}
