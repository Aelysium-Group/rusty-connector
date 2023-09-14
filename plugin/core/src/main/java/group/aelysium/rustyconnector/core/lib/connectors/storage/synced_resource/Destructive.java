package group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods marked with this annotation will destroy this {@link SyncedResource} once run.
 * Once a {@link SyncedResource} is destroyed, any calls to its methods will cause an exception.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Destructive {
}
