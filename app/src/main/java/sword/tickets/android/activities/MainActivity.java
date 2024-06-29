package sword.tickets.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import sword.collections.ImmutableMap;
import sword.tickets.android.DbManager;
import sword.tickets.android.Intentions;
import sword.tickets.android.R;
import sword.tickets.android.collections.MutableBitSet;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.layout.MainLayoutForActivity;
import sword.tickets.android.list.adapters.MainAdapter;
import sword.tickets.android.list.models.TicketEntry;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class MainActivity extends Activity {

    private static final int REQUEST_CODE_NEW_TICKET = 1;

    private interface SavedKeys {
        String STATE = "st";
    }

    private ImmutableMap<TicketId, String> _tickets;

    private MainLayoutForActivity _layout;
    private MainAdapter _adapter;
    private State _state;

    private boolean _uiJustUpdated;
    private ActionMode _actionMode;

    private void updateModelAndUi() {
        _tickets = DbManager.getInstance().getManager().getAllTickets();
        if (_state.selected.isEmpty()) {
            _adapter.setEntries(_tickets.toList().map(name -> new TicketEntry(name, false)));
        }
        else {
            _adapter.setEntries(_tickets.indexes().map(position -> new TicketEntry(_tickets.valueAt(position), _state.selected.contains(position))));
        }

        _uiJustUpdated = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _layout = MainLayoutForActivity.attach(this);

        if (savedInstanceState == null) {
            _state = new State(MutableBitSet.empty());
        }
        else {
            _state = savedInstanceState.getParcelable(SavedKeys.STATE, State.class);
        }
        ensureValidState(_state != null);

        _adapter = new MainAdapter();
        final ListView listView = _layout.listView();
        listView.setAdapter(_adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (_state.selected.isEmpty()) {
                TicketActivity.open(this, _tickets.keyAt(position));
            }
            else {
                _state.selected.flip(position);
                if (_state.selected.isEmpty()) {
                    if (_actionMode != null) {
                        _actionMode.finish();
                    }

                    _adapter.setEntries(_tickets.toList().map(name -> new TicketEntry(name, false)));
                }
                else {
                    if (_actionMode != null) {
                        _actionMode.setTitle("" + _state.selected.size());
                    }

                    _adapter.setEntries(_tickets.indexes().map(p -> new TicketEntry(_tickets.valueAt(p), _state.selected.contains(p))));
                }
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (_state.selected.isEmpty()) {
                _state.selected.add(position);
                _adapter.setEntries(_tickets.indexes().map(p -> new TicketEntry(_tickets.valueAt(p), p == position)));
                startActionMode(new ActionModeCallback());

                return true;
            }

            return false;
        });

        updateModelAndUi();
        if (!_state.selected.isEmpty()) {
            startActionMode(new ActionModeCallback());
            if (_state.displayingDeleteConfirmationDialog) {
                displayDeleteConfirmationDialog();
            }
        }
    }

    private void displayDeleteConfirmationDialog() {
        _state.displayingDeleteConfirmationDialog = true;
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.deleteTicketConfirmationDialogMessage, _state.selected.size()))
                .setPositiveButton(R.string.optionDelete, (dialog, which) -> {
                    deleteSelectedTickets();
                })
                .setNegativeButton(R.string.optionCancel, (dialog, which) -> {
                    // Nothing to be done
                })
                .setOnDismissListener(dialog -> {
                    _state.displayingDeleteConfirmationDialog = false;
                })
                .create().show();
    }

    private void deleteSelectedTickets() {
        boolean somethingDeleted = false;
        boolean errorFound = false;
        for (int position : _state.selected) {
            if (DbManager.getInstance().getManager().deleteTicket(_tickets.keyAt(position))) {
                somethingDeleted = true;
            }
            else {
                errorFound = true;
            }
        }

        if (errorFound) {
            Toast.makeText(MainActivity.this, R.string.unableToDeleteTickets, Toast.LENGTH_SHORT).show();
        }
        else if (somethingDeleted) {
            Toast.makeText(MainActivity.this, R.string.deletedTicketsOkFeedback, Toast.LENGTH_SHORT).show();
        }

        _state.selected.clear();
        if (somethingDeleted) {
            updateModelAndUi();
        }
        else {
            _adapter.setEntries(_tickets.toList().map(name -> new TicketEntry(name, false)));
        }

        _actionMode.finish();
    }

    private final class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            _actionMode = mode;
            mode.setTitle("1");
            getMenuInflater().inflate(R.menu.main_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.optionDelete) {
                displayDeleteConfirmationDialog();
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (_state.selected.clear()) {
                _adapter.setEntries(_tickets.toList().map(name -> new TicketEntry(name, false)));
            }

            _actionMode = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!_uiJustUpdated) {
            updateModelAndUi();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.optionNew) {
            Intentions.createTicket(this, REQUEST_CODE_NEW_TICKET);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SavedKeys.STATE, _state);
    }

    @Override
    protected void onPause() {
        _uiJustUpdated = false;
        super.onPause();
    }

    private static final class State implements Parcelable {
        @NonNull
        final MutableBitSet selected;
        boolean displayingDeleteConfirmationDialog;

        State(@NonNull MutableBitSet selected) {
            ensureNonNull(selected);
            this.selected = selected;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            if (selected.isEmpty()) {
                dest.writeInt(0);
            }
            else {
                final int max = selected.max();
                final int pageCount = max / 32 + 1;
                dest.writeInt(pageCount);

                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    int value = 0;
                    for (int bitIndex = 0; bitIndex < 32; bitIndex++) {
                        if (selected.contains(pageIndex * 32 + bitIndex)) {
                            value |= 1 << bitIndex;
                        }
                    }
                    dest.writeInt(value);
                }

                dest.writeBoolean(displayingDeleteConfirmationDialog);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                final int pageCount = in.readInt();
                final MutableBitSet selected = MutableBitSet.empty();
                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    final int value = in.readInt();
                    for (int bitIndex = 0; bitIndex < 32; bitIndex++) {
                        if ((value & (1 << bitIndex)) != 0) {
                            selected.add(pageIndex * 32 + bitIndex);
                        }
                    }
                }

                final State state = new State(selected);
                if (pageCount != 0) {
                    state.displayingDeleteConfirmationDialog = in.readBoolean();
                }

                return state;
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };
    }
}
