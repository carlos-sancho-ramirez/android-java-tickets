package sword.tickets.android;

import android.os.Build;

public final class ApiUtils {

    public static boolean isAtLeastApiLevel35() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM;
    }

    private ApiUtils() {
    }
}
