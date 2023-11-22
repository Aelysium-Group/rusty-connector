package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.toolkit.velocity.family.IResolvableFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;

public class ResolvableFamily implements IResolvableFamily {
    protected String name;

    public ResolvableFamily(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public BaseFamily resolve() {
        return new FamilyReference(this.name).get();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        ResolvableFamily that = (ResolvableFamily) object;
        return this.name.equals(that.name());
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static ResolvableFamily from(BaseFamily family) {
        return new ResolvableFamily(family.name());
    }
}
