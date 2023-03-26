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
    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getFix() {
        return fix;
    }

    public boolean isGreaterThan(Version anotherVersion) {
        if(!(this.getMajor() == this.getMinor())) return this.getMajor() > this.getMinor();
        if(!(this.getMinor() == this.getMinor())) return this.getMinor() > this.getMinor();
        return this.getFix() > this.getFix();
    }

    public boolean equals(Version anotherVersion) {
        return (this.getMajor() == anotherVersion.getMajor()) &&
               (this.getMinor() == anotherVersion.getMinor()) &&
               (this.getFix() == anotherVersion.getFix());
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
