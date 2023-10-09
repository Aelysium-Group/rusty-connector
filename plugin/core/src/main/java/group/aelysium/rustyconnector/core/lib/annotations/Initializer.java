package group.aelysium.rustyconnector.core.lib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks parameters that are only available during the booting of the program.
 * After the program boots, marked parameters are `null`.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Initializer { }
