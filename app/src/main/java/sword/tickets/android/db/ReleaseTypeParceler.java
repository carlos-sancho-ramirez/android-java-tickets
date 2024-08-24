package sword.tickets.android.db;

import android.os.Parcel;

import androidx.annotation.NonNull;

import sword.tickets.android.db.TicketsDbSchema.ReleaseType;

public final class ReleaseTypeParceler {

    public static ReleaseType read(@NonNull Parcel in) {
        final int rawValue = in.readInt();
        if (rawValue == 0) {
            return null;
        }
        else if (rawValue == ReleaseType.MAJOR.value) {
            return ReleaseType.MAJOR;
        }
        else if (rawValue == ReleaseType.MINOR.value) {
            return ReleaseType.MINOR;
        }
        else if (rawValue == ReleaseType.BUG_FIX.value) {
            return ReleaseType.BUG_FIX;
        }
        else {
            throw new UnsupportedOperationException("Unrecognised value '" + rawValue + "' as releaseType");
        }
    }

    public static void write(@NonNull Parcel out, ReleaseType releaseType) {
        out.writeInt((releaseType != null)? releaseType.value : 0);
    }

    private ReleaseTypeParceler() {
    }
}
