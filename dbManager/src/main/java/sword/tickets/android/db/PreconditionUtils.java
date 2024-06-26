package sword.tickets.android.db;

final class PreconditionUtils {
    static void ensureNonNull(Object... instances) {
        for (Object instance : instances) {
            if (instance == null) {
                throw new IllegalArgumentException();
            }
        }
    }

    static void ensureValidState(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    private PreconditionUtils() {
    }
}
