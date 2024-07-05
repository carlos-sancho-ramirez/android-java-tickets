package sword.tickets.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableMap;
import sword.tickets.android.DbManager;
import sword.tickets.android.Intentions;
import sword.tickets.android.R;
import sword.tickets.android.UserPreferences;
import sword.tickets.android.collections.MutableBitSet;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.db.TicketIdBundler;
import sword.tickets.android.db.TicketsDbManagerImpl;
import sword.tickets.android.layout.MainLayoutForActivity;
import sword.tickets.android.list.adapters.MainAdapter;
import sword.tickets.android.list.adapters.ProjectPickerAdapter;
import sword.tickets.android.list.models.TicketEntry;
import sword.tickets.android.models.Ticket;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class MainActivity extends Activity {

    private static final int REQUEST_CODE_NEW_TICKET = 1;

    private interface SavedKeys {
        String STATE = "st";
    }

    private MainLayoutForActivity _layout;
    private ImmutableMap<ProjectId, String> _projects;
    private ProjectId _selectedProjectId;
    private ImmutableMap<TicketId, String> _tickets;

    private MainAdapter _adapter;
    private State _state;

    private boolean _dataInSync;
    private int _lastDataVersion;
    private ActionMode _actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _layout = MainLayoutForActivity.attach(this);

        final TicketsDbManagerImpl manager = DbManager.getInstance().getManager();
        _projects = manager.getAllProjects();

        final UserPreferences userPreferences = new UserPreferences(this);
        _selectedProjectId = userPreferences.getSelectedProject();

        final int projectCount = _projects.size();
        if (projectCount == 0) {
            if (_selectedProjectId != null) {
                userPreferences.setSelectedProject(null);
                _selectedProjectId = null;
            }

            _tickets = ImmutableHashMap.empty();
        }
        else if (projectCount == 1) {
            if (!_projects.keyAt(0).equals(_selectedProjectId)) {
                _selectedProjectId = _projects.keyAt(0);
                userPreferences.setSelectedProject(_selectedProjectId);
            }

            _tickets = manager.getAllTickets();
        }
        else {
            if (!_projects.containsKey(_selectedProjectId)) {
                _selectedProjectId = _projects.keyAt(0);
                userPreferences.setSelectedProject(_selectedProjectId);
            }

            _tickets = manager.getAllTicketsForProject(_selectedProjectId);
        }
        ensureValidState(_tickets != null);

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

        final Spinner projectSpinner = _layout.projectSpinner();
        if (projectCount >= 2) {
            final int selectedPosition = _projects.indexOfKey(_selectedProjectId);
            projectSpinner.setAdapter(new ProjectPickerAdapter(_projects.toList()));
            projectSpinner.setSelection(selectedPosition);
            projectSpinner.setOnItemSelectedListener(new ProjectSelectionChangedListener());
        }
        else {
            projectSpinner.setVisibility(View.GONE);
        }

        if (_state.selected.isEmpty()) {
            _adapter.setEntries(_tickets.toList().map(name -> new TicketEntry(name, false)));
        }
        else {
            _adapter.setEntries(_tickets.indexes().map(position -> new TicketEntry(_tickets.valueAt(position), _state.selected.contains(position))));

            startActionMode(new ActionModeCallback());
            if (_state.displayingDeleteConfirmationDialog) {
                displayDeleteConfirmationDialog();
            }
        }

        _lastDataVersion = DbManager.getInstance().getDatabase().getDataVersion();
        _dataInSync = true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NEW_TICKET && resultCode == Activity.RESULT_OK && !_dataInSync) {
            final TicketsDbManagerImpl manager = DbManager.getInstance().getManager();
            final TicketId newTicketId = TicketIdBundler.readAsIntentExtra(data, Intentions.ResultKeys.TICKET_ID);
            final Ticket<ProjectId> newTicket = manager.getTicket(newTicketId);
            final boolean selectedProjectChanged = !newTicket.projectId.equals(_selectedProjectId);
            if (selectedProjectChanged) {
                _selectedProjectId = newTicket.projectId;
                new UserPreferences(this).setSelectedProject(_selectedProjectId);
            }

            final ImmutableMap<ProjectId, String> previousProjects = _projects;
            _projects = manager.getAllProjects();
            final int projectCount = _projects.size();

            if (!_projects.equalMap(previousProjects)) {
                final Spinner projectSpinner = _layout.projectSpinner();
                if (projectCount >= 2) {
                    if (previousProjects.size() >= 2) {
                        projectSpinner.setOnItemSelectedListener(null);
                    }
                    else {
                        projectSpinner.setVisibility(View.VISIBLE);
                    }

                    final int selectedPosition = _projects.indexOfKey(_selectedProjectId);
                    projectSpinner.setAdapter(new ProjectPickerAdapter(_projects.toList()));
                    projectSpinner.setSelection(selectedPosition);
                    projectSpinner.setOnItemSelectedListener(new ProjectSelectionChangedListener());
                }
            }

            _tickets = (projectCount == 1)? manager.getAllTickets() : manager.getAllTicketsForProject(_selectedProjectId);
            _adapter.setEntries(_tickets.toList().map(name -> new TicketEntry(name, false)));
            _lastDataVersion = DbManager.getInstance().getDatabase().getDataVersion();
            _dataInSync = true;
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
        final TicketsDbManagerImpl manager = DbManager.getInstance().getManager();
        for (int position : _state.selected) {
            if (manager.deleteTicket(_tickets.keyAt(position))) {
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
            _tickets = manager.getAllTicketsForProject(_selectedProjectId);
            _adapter.setEntries(_tickets.toList().map(name -> new TicketEntry(name, false)));
        }
        else {
            _adapter.setEntries(_tickets.toList().map(name -> new TicketEntry(name, false)));
        }

        _actionMode.finish();
        _lastDataVersion = DbManager.getInstance().getDatabase().getDataVersion();
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
        final DbManager dbManager = DbManager.getInstance();
        if (!_dataInSync && dbManager.getDatabase().getDataVersion() != _lastDataVersion) {
            final int projectCount = _projects.size();
            if (projectCount == 0) {
                _tickets = ImmutableHashMap.empty();
            }
            else if (projectCount == 1) {
                _tickets = dbManager.getManager().getAllTickets();
            }
            else {
                _tickets = dbManager.getManager().getAllTicketsForProject(_selectedProjectId);
            }
            _adapter.setEntries(_tickets.toList().map(name -> new TicketEntry(name, false)));
            _lastDataVersion = dbManager.getDatabase().getDataVersion();
            _dataInSync = true;
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
    protected void onPause() {
        _dataInSync = false;
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SavedKeys.STATE, _state);
    }

    private final class ProjectSelectionChangedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final ProjectId newSelection = _projects.keyAt(position);
            if (!newSelection.equals(_selectedProjectId)) {
                _selectedProjectId = newSelection;
                new UserPreferences(MainActivity.this).setSelectedProject(newSelection);
                _tickets = DbManager.getInstance().getManager().getAllTicketsForProject(newSelection);
                _adapter.setEntries(_tickets.toList().map(name -> new TicketEntry(name, false)));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Nothing to be done
        }
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
