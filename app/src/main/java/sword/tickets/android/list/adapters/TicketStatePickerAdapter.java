package sword.tickets.android.list.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import sword.collections.ImmutableIntValueHashMap;
import sword.collections.ImmutableIntValueMap;
import sword.tickets.android.R;
import sword.tickets.android.db.TicketsDbSchema.TicketState;
import sword.tickets.android.layout.PickerEntryLayout;

import androidx.annotation.NonNull;

import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class TicketStatePickerAdapter extends BaseAdapter {

    public static final ImmutableIntValueMap<TicketState> STATE_TEXTS = new ImmutableIntValueHashMap.Builder<TicketState>()
            .put(TicketState.NOT_STARTED, R.string.ticketStateNotStarted)
            .put(TicketState.IN_PROGRESS, R.string.ticketStateInProgress)
            .put(TicketState.ABANDONED, R.string.ticketStateAbandoned)
            .put(TicketState.COMPLETED, R.string.ticketStateCompleted)
            .build();

    private LayoutInflater _inflater;

    @Override
    public int getCount() {
        return TicketState.values().length;
    }

    @NonNull
    @Override
    public TicketState getItem(int position) {
        return TicketState.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return TicketState.values()[position].value;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final PickerEntryLayout layout;
        if (view == null) {
            if (_inflater == null) {
                _inflater = LayoutInflater.from(parent.getContext());
            }

            layout = PickerEntryLayout.createWithLayoutInflater(_inflater, parent);
            view = layout.view();
            view.setTag(R.id.layoutTagForView, layout);
        }
        else {
            layout = (PickerEntryLayout) view.getTag(R.id.layoutTagForView);
            ensureValidState(layout != null);
        }

        layout.textView().setText(STATE_TEXTS.get(getItem(position)));
        return view;
    }
}
