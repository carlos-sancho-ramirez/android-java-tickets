package sword.tickets.android.db;

public final class PreconditionUtils {
    public static void ensureNonNull(Object... instances) {
        for (Object instance : instances) {
            if (instance == null) {
                throw new IllegalArgumentException();
            }
        }
    }

    public static void ensureValidArguments(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    public static void ensureValidState(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    private PreconditionUtils() {
    }
}
