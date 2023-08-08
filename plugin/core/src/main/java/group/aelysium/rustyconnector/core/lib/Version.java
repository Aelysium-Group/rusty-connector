package group.aelysium.rustyconnector.core.lib;

public class Version {
    protected int major;
    protected int minor;
    protected int fix;

    public Version(int major, int minor, int fix) {
        this.major = major;
        this.minor = minor;
        this.fix = fix;
    }
    public Version(String string) throws NumberFormatException{
        String[] stringSplit = string.split("\\.");
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
}
