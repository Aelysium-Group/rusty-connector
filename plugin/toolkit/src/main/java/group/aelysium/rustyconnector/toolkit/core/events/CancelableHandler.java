package group.aelysium.rustyconnector.toolkit.core.events;

import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.IncludeFilters;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IncludeFilters({@Filter(CancelFilter.class)})
@Retention(RetentionPolicy.RUNTIME)
public @interface CancelableHandler {
}
