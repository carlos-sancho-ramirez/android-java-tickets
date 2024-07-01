package sword.tickets.android.list.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import sword.collections.ImmutableList;
import sword.tickets.android.R;
import sword.tickets.android.db.TicketsDbSchema;
import sword.tickets.android.layout.TicketTypeEntryLayout;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class TicketTypeAdapter extends BaseAdapter {

    public static final class Entry {
        @NonNull
        public final TicketsDbSchema.TicketType type;

        @StringRes
        public final int name;

        private Entry(@NonNull TicketsDbSchema.TicketType type, @StringRes int name) {
            ensureNonNull(type);
            this.type = type;
            this.name = name;
        }
    }

    private static final ImmutableList<Entry> _entries = new ImmutableList.Builder<Entry>()
            .append(new Entry(TicketsDbSchema.TicketType.NEW_CAPABILITY, R.string.ticketTypeNewCapability))
            .append(new Entry(TicketsDbSchema.TicketType.ISSUE, R.string.ticketTypeIssue))
            .append(new Entry(TicketsDbSchema.TicketType.MODIFICATION, R.string.ticketTypeModification))
            .build();

    private LayoutInflater _inflater;

    @Override
    public int getCount() {
        return _entries.size();
    }

    @Override
    public Entry getItem(int position) {
        return _entries.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return _entries.valueAt(position).type.value;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final TicketTypeEntryLayout layout;
        if (view == null) {
            if (_inflater == null) {
                _inflater = LayoutInflater.from(parent.getContext());
            }

            layout = TicketTypeEntryLayout.createWithLayoutInflater(_inflater, parent);
            view = layout.view();
            view.setTag(R.id.layoutTagForView, layout);
        }
        else {
            layout = (TicketTypeEntryLayout) view.getTag(R.id.layoutTagForView);
            ensureValidState(layout != null);
        }

        layout.view().setText(_entries.valueAt(position).name);
        return view;
    }
}
