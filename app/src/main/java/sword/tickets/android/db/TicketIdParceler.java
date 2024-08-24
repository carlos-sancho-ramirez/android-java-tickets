package sword.tickets.android.db;

import android.os.Parcel;

import androidx.annotation.NonNull;

public final class TicketIdParceler {

    public static TicketId read(@NonNull Parcel in) {
        final int rawId = in.readInt();
        return (rawId != 0)? new TicketId(rawId) : null;
    }

    public static void write(@NonNull Parcel out, TicketId id) {
        final int rawId = (id != null)? id.key : 0;
        out.writeInt(rawId);
    }

    private TicketIdParceler() {
    }
}
