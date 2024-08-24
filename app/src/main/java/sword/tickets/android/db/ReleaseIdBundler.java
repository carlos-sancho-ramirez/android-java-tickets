package sword.tickets.android.db;

import android.content.Intent;

import androidx.annotation.NonNull;

public final class ReleaseIdBundler {

    public static ReleaseId readAsIntentExtra(@NonNull Intent intent, @NonNull String key) {
        final int idKey = intent.getIntExtra(key, 0);
        return (idKey != 0)? new ReleaseId(idKey) : null;
    }

    public static void writeAsIntentExtra(@NonNull Intent intent, @NonNull String key, ReleaseId releaseId) {
        if (releaseId != null) {
            intent.putExtra(key, releaseId.key);
        }
    }

    private ReleaseIdBundler() {
    }
}
