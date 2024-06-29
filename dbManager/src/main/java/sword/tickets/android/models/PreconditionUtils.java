package sword.tickets.android.models;

final class PreconditionUtils {
    static void ensureNonNull(Object... instances) {
        for (Object instance : instances) {
            if (instance == null) {
                throw new IllegalArgumentException();
            }
        }
    }

    private PreconditionUtils() {
    }
}
