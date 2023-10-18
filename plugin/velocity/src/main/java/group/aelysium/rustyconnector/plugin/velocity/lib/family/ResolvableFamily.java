package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;

import java.util.Objects;
import java.util.Optional;

public record ResolvableFamily(String name) {
    public Optional<BaseServerFamily<?>> resolve() {
        BaseServerFamily<?> potentialFamily = Tinder.get().services().familyService().find(this.name);
        if(potentialFamily == null) return Optional.empty();
        return Optional.of(potentialFamily);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        ResolvableFamily that = (ResolvableFamily) object;
        return this.name.equals(that.name());
    }
    public static ResolvableFamily from(BaseServerFamily<?> family) {
        return new ResolvableFamily(family.name());
    }
}
