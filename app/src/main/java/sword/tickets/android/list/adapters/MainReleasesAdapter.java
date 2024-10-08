package sword.tickets.android.list.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.tickets.android.R;
import sword.tickets.android.layout.PickerEntryLayout;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class MainReleasesAdapter extends BaseAdapter {
    @NonNull
    private ImmutableList<String> _entries = ImmutableList.empty();
    private LayoutInflater _inflater;

    public void setEntries(@NonNull ImmutableList<String> entries) {
        ensureNonNull(entries);
        _entries = entries;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return _entries.size();
    }

    @Override
    public String getItem(int position) {
        return _entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup viewGroup) {
        final PickerEntryLayout layout;
        if (view == null) {
            if (_inflater == null) {
                _inflater = LayoutInflater.from(viewGroup.getContext());
            }

            layout = PickerEntryLayout.createWithLayoutInflater(_inflater, viewGroup);
            view = layout.view();
            view.setTag(R.id.layoutTagForView, layout);
        }
        else {
            layout = (PickerEntryLayout) view.getTag(R.id.layoutTagForView);
            ensureValidState(layout != null);
        }

        layout.textView().setText(_entries.valueAt(position));
        return view;
    }
}
