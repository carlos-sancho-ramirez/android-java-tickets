package sword.tickets.android.list.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.tickets.android.R;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;

public final class MainAdapter extends BaseAdapter {
    @NonNull
    private final ImmutableList<String> _entries;
    private LayoutInflater _inflater;

    public MainAdapter(@NonNull ImmutableList<String> entries) {
        ensureNonNull(entries);
        _entries = entries;
    }

    @Override
    public int getCount() {
        return _entries.size();
    }

    @Override
    public String getItem(int i) {
        return _entries.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup viewGroup) {
        if (view == null) {
            if (_inflater == null) {
                _inflater = LayoutInflater.from(viewGroup.getContext());
            }

            view = _inflater.inflate(R.layout.main_entry, viewGroup, false);
        }

        ((TextView) view).setText(_entries.valueAt(position));
        return view;
    }
}
