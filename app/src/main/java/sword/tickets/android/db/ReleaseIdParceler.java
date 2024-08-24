package sword.tickets.android.db;

import android.os.Parcel;

import androidx.annotation.NonNull;

public final class ReleaseIdParceler {

    public static ReleaseId read(@NonNull Parcel in) {
        final int rawId = in.readInt();
        return (rawId != 0)? new ReleaseId(rawId) : null;
    }

    public static void write(@NonNull Parcel out, ReleaseId id) {
        final int rawId = (id != null)? id.key : 0;
        out.writeInt(rawId);
    }

    private ReleaseIdParceler() {
    }
}
