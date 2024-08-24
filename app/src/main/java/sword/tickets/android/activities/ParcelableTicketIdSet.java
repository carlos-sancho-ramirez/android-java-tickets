package sword.tickets.android.activities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableSet;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.db.TicketIdParceler;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;

public final class ParcelableTicketIdSet implements Parcelable {

    @NonNull
    private final ImmutableSet<TicketId> _set;

    public ParcelableTicketIdSet(@NonNull ImmutableSet<TicketId> set) {
        ensureNonNull(set);
        _set = set;
    }

    @NonNull
    public ImmutableSet<TicketId> get() {
        return _set;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(_set.size());
        for (TicketId ticketId : _set) {
            TicketIdParceler.write(dest, ticketId);
        }
    }

    public static final Creator<ParcelableTicketIdSet> CREATOR = new Creator<ParcelableTicketIdSet>() {
        @NonNull
        @Override
        public ParcelableTicketIdSet createFromParcel(Parcel in) {
            final int size = in.readInt();
            final ImmutableHashSet.Builder<TicketId> builder = new ImmutableHashSet.Builder<>();
            for (int i = 0; i < size; i++) {
                builder.add(TicketIdParceler.read(in));
            }

            return new ParcelableTicketIdSet(builder.build());
        }

        @NonNull
        @Override
        public ParcelableTicketIdSet[] newArray(int size) {
            return new ParcelableTicketIdSet[size];
        }
    };
}
