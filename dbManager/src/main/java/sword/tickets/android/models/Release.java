package sword.tickets.android.models;

public final class Release {
    public final int major;
    public final int minor;
    public final int bugFix;

    public Release(int major, int minor, int bugFix) {
        this.major = major;
        this.minor = minor;
        this.bugFix = bugFix;
    }
}
