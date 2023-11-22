package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;

public class FamilyReference {
    private boolean rootFamily = false;
    protected String name = "";

    public FamilyReference(String name) {
        this.name = name;
    }
    protected FamilyReference() {
        this.rootFamily = true;
    }

    /**
     * Gets the family referenced.
     * If no family could be found, this will throw an exception.
     * @return {@link BaseFamily}
     * @throws java.util.NoSuchElementException If the family can't be found.
     */
    public BaseFamily get() {
        if(rootFamily) return Tinder.get().services().family().rootFamily();
        return Tinder.get().services().family().find(this.name).orElseThrow();
    }

    /**
     * Gets the family referenced.
     * If no family could be found and {@param fetchRoot} is disabled, will throw an exception.
     * If {@param fetchRoot} is enabled and the family isn't found, will return the root family instead.
     * @param fetchRoot Should the root family be returned if the parent family can't be found?
     * @return {@link BaseFamily}
     * @throws java.util.NoSuchElementException If {@param fetchRoot} is disabled and the family can't be found.
     */
    public BaseFamily get(boolean fetchRoot) {
        if(rootFamily) return Tinder.get().services().family().rootFamily();
        if(fetchRoot)
            try {
                return Tinder.get().services().family().find(this.name).orElseThrow();
            } catch (Exception ignore) {
                return Tinder.get().services().family().rootFamily();
            }
        else return Tinder.get().services().family().find(this.name).orElseThrow();
    }

    public static FamilyReference rootFamily() {
        return new FamilyReference();
    }
}
