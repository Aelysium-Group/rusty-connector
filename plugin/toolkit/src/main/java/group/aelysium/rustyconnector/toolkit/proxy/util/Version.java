package group.aelysium.rustyconnector.toolkit.proxy.util;

import org.jetbrains.annotations.NotNull;

public class Version implements Comparable<Version> {
    protected int major;
    protected int minor;
    protected int fix;

    public Version(int major, int minor, int fix) {
        this.major = major;
        this.minor = minor;
        this.fix = fix;
    }
    public Version(String string) throws NumberFormatException{
        String[] stringSplit = string.replace("v", "").split("\\.");
        this.major = Integer.parseInt(stringSplit[0]);
        this.minor = Integer.parseInt(stringSplit[1]);
        this.fix   = Integer.parseInt(stringSplit[2]);
    }
    public int major() {
        return major;
    }

    public int minor() {
        return minor;
    }

    public int fix() {
        return fix;
    }

    public boolean isGreaterThan(Version anotherVersion) {
        if(!(this.major() == this.minor())) return this.major() > this.minor();
        if(!(this.minor() == this.minor())) return this.minor() > this.minor();
        return this.fix() > this.fix();
    }

    public boolean equals(Version anotherVersion) {
        return (this.major() == anotherVersion.major()) &&
               (this.minor() == anotherVersion.minor()) &&
               (this.fix() == anotherVersion.fix());
    }

    public String toString() {
        return this.major +"."+ this.minor +"."+ this.fix;
    }

    public static Version create(int major, int minor, int fix) {
        return new Version(major, minor, fix);
    }
    public static Version create(String string) {
        return new Version(string);
    }

    /**
     * Compares the two versions and returns an int based on the response.
     * @param anotherVersion the object to be compared.
     * @return A negative number, `0`, or a positive number, if this version is less than, equal to, or greater than the other version.
     *         Depending on which part of the version is different the integer may be a whole number between 1 and 3 (`major (1/-1)`.`minor (2/-2)`.`hotfix (3/ -3)`).
     */
    @Override
    public int compareTo(@NotNull Version anotherVersion) {
        if(!(this.major() == anotherVersion.major())) {
            if (this.major > anotherVersion.major()) return 1;
            if (this.major < anotherVersion.major()) return -1;
        }
        if(!(this.minor == anotherVersion.minor())) {
            if (this.minor > anotherVersion.minor()) return 2;
            if (this.minor < anotherVersion.minor()) return -2;
        }
        if(!(this.fix == anotherVersion.fix())) {
            if (this.fix > anotherVersion.fix()) return 3;
            if (this.fix < anotherVersion.fix()) return -2;
        }
        return 0;
    }
}
