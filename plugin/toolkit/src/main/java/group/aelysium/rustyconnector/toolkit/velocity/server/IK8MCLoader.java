package group.aelysium.rustyconnector.toolkit.velocity.server;

public interface IK8MCLoader extends MCLoader {
    /**
     * Gets the pod name associated with this MCLoader.
     * @return The pod name, never null.
     */
    String podName();
}
