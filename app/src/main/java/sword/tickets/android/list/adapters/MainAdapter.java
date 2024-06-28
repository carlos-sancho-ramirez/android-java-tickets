package sword.tickets.android.list.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.tickets.android.R;
import sword.tickets.android.layout.MainEntryLayout;
import sword.tickets.android.list.models.TicketEntry;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class MainAdapter extends BaseAdapter {
    @NonNull
    private ImmutableList<TicketEntry> _entries = ImmutableList.empty();
    private LayoutInflater _inflater;

    public void setEntries(@NonNull ImmutableList<TicketEntry> entries) {
        ensureNonNull(entries);
        _entries = entries;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return _entries.size();
    }

    @Override
    public TicketEntry getItem(int position) {
        return _entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup viewGroup) {
        final MainEntryLayout layout;
        if (view == null) {
            if (_inflater == null) {
                _inflater = LayoutInflater.from(viewGroup.getContext());
            }

            layout = MainEntryLayout.createWithLayoutInflater(_inflater, viewGroup);
            view = layout.view();
            view.setTag(R.id.layoutTagForView, layout);
        }
        else {
            layout = (MainEntryLayout) view.getTag(R.id.layoutTagForView);
            ensureValidState(layout != null);
        }

        final TicketEntry entry = _entries.valueAt(position);
        layout.textView().setText(entry.name);
        layout.textView().setBackgroundColor(entry.selected? 0x663366FF : 0);
        return view;
    }
}
