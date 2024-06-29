package sword.tickets.android.db;

import android.os.Parcel;

import androidx.annotation.NonNull;

public final class ProjectIdParceler {

    public static ProjectId read(@NonNull Parcel in) {
        final int rawId = in.readInt();
        return (rawId != 0)? new ProjectId(rawId) : null;
    }

    public static void write(@NonNull Parcel out, ProjectId id) {
        final int rawId = (id != null)? id.key : 0;
        out.writeInt(rawId);
    }

    private ProjectIdParceler() {
    }
}
