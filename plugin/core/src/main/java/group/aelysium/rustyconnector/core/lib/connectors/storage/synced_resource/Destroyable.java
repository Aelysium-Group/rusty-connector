package group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods marked with this annotation will be destroyed once a {@link Destructive} method is run.
 * If a method marked with this annotation is destroyed, it will throw an exception when run.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Destroyable {
}
