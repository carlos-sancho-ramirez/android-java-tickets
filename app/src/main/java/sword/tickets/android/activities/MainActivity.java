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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.tickets.android.DbManager;
import sword.tickets.android.Intentions;
import sword.tickets.android.R;
import sword.tickets.android.UserPreferences;
import sword.tickets.android.collections.MutableBitSet;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.ReleaseId;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.db.TicketIdBundler;
import sword.tickets.android.db.TicketsDbManagerImpl;
import sword.tickets.android.layout.MainLayoutForActivity;
import sword.tickets.android.layout.NoProjectsPlaceholderLayout;
import sword.tickets.android.layout.ReleasesTabLayout;
import sword.tickets.android.layout.TicketsTabLayout;
import sword.tickets.android.list.adapters.MainReleasesAdapter;
import sword.tickets.android.list.adapters.MainTicketsAdapter;
import sword.tickets.android.list.adapters.ProjectPickerAdapter;
import sword.tickets.android.list.models.TicketEntry;
import sword.tickets.android.models.Release;
import sword.tickets.android.models.Ticket;
import sword.tickets.android.models.TicketReference;
import sword.tickets.android.view.ListViewFrameView;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;
import static sword.tickets.android.activities.ActivityUtils.applyMainInsets;

public final class MainActivity extends Activity {

    private static final int REQUEST_CODE_NEW_RELEASE = 1;
    private static final int REQUEST_CODE_NEW_TICKET = 2;

    private interface SavedKeys {
        String STATE = "st";
    }

    private MainLayoutForActivity _layout;
    private ImmutableMap<ProjectId, String> _projects;
    private ProjectId _selectedProjectId;
    private ImmutableList<TicketReference<TicketId>> _tickets;
    private ImmutableList<ReleaseFullEntry> _releases;

    private TicketsTabLayout _ticketsTabLayout;
    private ReleasesTabLayout _releasesTabLayout;
    private NoProjectsPlaceholderLayout _noProjectsLayout;

    private MainTicketsAdapter _ticketsAdapter;
    private State _state;

    private MainReleasesAdapter _releasesAdapter;

    private boolean _dataInSync;
    private int _lastDataVersion;
    private ActionMode _actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _layout = MainLayoutForActivity.attach(this);
        applyMainInsets(_layout.mainContainer());

        if (savedInstanceState == null) {
            _state = new State(MutableBitSet.empty());
        }
        else {
            _state = savedInstanceState.getParcelable(SavedKeys.STATE, State.class);
        }
        ensureValidState(_state != null);

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
        }
        else {
            _layout.tabBar().setVisibility(View.VISIBLE);
            _layout.ticketsTab().setOnClickListener(v -> {
                _layout.releasesTab().setSelected(false);
                _layout.ticketsTab().setSelected(true);

                if (_state.releasesTabSelected) {
                    _state.releasesTabSelected = false;
                    if (_tickets == null) {
                        if (_projects.isEmpty()) {
                            _tickets = ImmutableList.empty();
                        }
                        else if (_projects.size() == 1) {
                            _tickets = manager.getAllTicketReferences();
                        }
                        else {
                            _tickets = manager.getAllTicketReferencesForProject(_selectedProjectId);
                        }
                    }
                    ensureValidState(_tickets != null);

                    _layout.tabContent().removeAllViews();
                    if (_ticketsTabLayout == null) {
                        _ticketsTabLayout = TicketsTabLayout.attach(_layout.tabContent());

                        _ticketsAdapter = new MainTicketsAdapter();
                        final ListView listView = _ticketsTabLayout.listView();
                        listView.setAdapter(_ticketsAdapter);

                        _ticketsTabLayout.listViewFrame().setLongClickListener(new LongTouchListener());

                        ensureValidState(_state.selected.isEmpty());
                        _ticketsAdapter.setEntries(_tickets.map(ticket -> new TicketEntry(ticket.name, false)));
                    }
                    else {
                        _layout.tabContent().addView(_ticketsTabLayout.view());
                    }
                }
            });

            _layout.releasesTab().setOnClickListener(v -> {
                _layout.ticketsTab().setSelected(false);
                _layout.releasesTab().setSelected(true);

                if (!_state.releasesTabSelected) {
                    _state.releasesTabSelected = true;
                    if (_releases == null) {
                        final ImmutableMap<ReleaseId, Release> releases = DbManager.getInstance().getManager().getAllReleasesForProject(_selectedProjectId);
                        _releases = releases.entries()
                                .map(entry -> new ReleaseFullEntry(entry.key(), entry.value().major, entry.value().minor, entry.value().bugFix))
                                .sort((a, b) -> a.major > b.major || a.major == b.major && (a.minor > b.minor || a.minor == b.minor && a.bugFix > b.bugFix));
                    }

                    _layout.tabContent().removeAllViews();
                    if (_releasesTabLayout == null) {
                        _releasesTabLayout = ReleasesTabLayout.attach(_layout.tabContent());
                        if (_tickets.isEmpty()) {
                            _releasesTabLayout.noReleasesPlaceholder().setVisibility(View.VISIBLE);
                        }
                        else {
                            _releasesAdapter = new MainReleasesAdapter();
                            _releasesAdapter.setEntries(_releases.map(e -> Integer.toString(e.major) + '.' + e.minor + '.' + e.bugFix));
                            final ListView listView = _releasesTabLayout.releasesListView();
                            listView.setAdapter(_releasesAdapter);
                            listView.setVisibility(View.VISIBLE);
                        }
                    }
                    else {
                        _layout.tabContent().addView(_releasesTabLayout.view());
                    }
                }
            });

            if (projectCount == 1) {
                if (!_projects.keyAt(0).equals(_selectedProjectId)) {
                    _selectedProjectId = _projects.keyAt(0);
                    userPreferences.setSelectedProject(_selectedProjectId);
                }
            }
            else if (!_projects.containsKey(_selectedProjectId)) {
                _selectedProjectId = _projects.keyAt(0);
                userPreferences.setSelectedProject(_selectedProjectId);
            }
        }

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

        if (_state.releasesTabSelected) {
            _layout.releasesTab().setSelected(true);
            _releasesTabLayout = ReleasesTabLayout.attach(_layout.tabContent());
            _releasesTabLayout.noReleasesPlaceholder().setVisibility(View.VISIBLE);
        }
        else {
            _layout.ticketsTab().setSelected(true);

            if (projectCount == 0) {
                _noProjectsLayout = NoProjectsPlaceholderLayout.attach(_layout.tabContent());
                _noProjectsLayout.newTicketButton().setOnClickListener(v ->
                        Intentions.createTicket(this, REQUEST_CODE_NEW_TICKET));
            }
            else {
                _ticketsTabLayout = TicketsTabLayout.attach(_layout.tabContent());
                _tickets = (projectCount == 1)? manager.getAllTicketReferences() :
                        manager.getAllTicketReferencesForProject(_selectedProjectId);
                ensureValidState(_tickets != null);

                _ticketsAdapter = new MainTicketsAdapter();
                final ListView listView = _ticketsTabLayout.listView();
                listView.setAdapter(_ticketsAdapter);

                _ticketsTabLayout.listViewFrame().setLongClickListener(new LongTouchListener());

                if (_state.selected.isEmpty()) {
                    _ticketsAdapter.setEntries(_tickets.map(ticket -> new TicketEntry(ticket.name, false)));
                }
                else {
                    _ticketsAdapter.setEntries(_tickets.indexes().map(position -> new TicketEntry(_tickets.valueAt(position).name, _state.selected.contains(position))));

                    startActionMode(new ActionModeCallback());
                    if (_state.displayingDeleteConfirmationDialog) {
                        displayDeleteConfirmationDialog();
                    }
                }
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
            final Ticket<ProjectId, ReleaseId> newTicket = manager.getTicket(newTicketId);
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

            _tickets = (projectCount == 1)? manager.getAllTicketReferences() : manager.getAllTicketReferencesForProject(_selectedProjectId);
            ensureValidState(_tickets != null);

            if (_ticketsTabLayout == null) {
                _layout.tabContent().removeAllViews();
                _ticketsTabLayout = TicketsTabLayout.attach(_layout.tabContent());

                _ticketsAdapter = new MainTicketsAdapter();
                final ListView listView = _ticketsTabLayout.listView();
                listView.setAdapter(_ticketsAdapter);
            }

            _ticketsAdapter.setEntries(_tickets.map(ticket -> new TicketEntry(ticket.name, false)));
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
            if (manager.deleteTicket(_tickets.valueAt(position).id)) {
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
        _ticketsTabLayout.listViewFrame().stopMultiSelection();
        if (somethingDeleted) {
            _tickets = manager.getAllTicketReferencesForProject(_selectedProjectId);
            _ticketsAdapter.setEntries(_tickets.map(tickets -> new TicketEntry(tickets.name, false)));
        }
        else {
            _ticketsAdapter.setEntries(_tickets.map(tickets -> new TicketEntry(tickets.name, false)));
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
                _ticketsAdapter.setEntries(_tickets.map(tickets -> new TicketEntry(tickets.name, false)));
            }

            _ticketsTabLayout.listViewFrame().stopMultiSelection();
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
                _tickets = ImmutableList.empty();
            }
            else if (projectCount == 1) {
                _tickets = dbManager.getManager().getAllTicketReferences();
            }
            else {
                _tickets = dbManager.getManager().getAllTicketReferencesForProject(_selectedProjectId);
            }
            _ticketsAdapter.setEntries(_tickets.map(tickets -> new TicketEntry(tickets.name, false)));
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
            if (_state.releasesTabSelected) {
                Intentions.createRelease(this, REQUEST_CODE_NEW_RELEASE);
            }
            else {
                Intentions.createTicket(this, REQUEST_CODE_NEW_TICKET);
            }
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
                _tickets = DbManager.getInstance().getManager().getAllTicketReferencesForProject(newSelection);
                _ticketsAdapter.setEntries(_tickets.map(tickets -> new TicketEntry(tickets.name, false)));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Nothing to be done
        }
    }

    private final class LongTouchListener implements ListViewFrameView.LongClickListener {

        private int _originalTopPosition;
        private int _originalHeight;
        private View _floatingView;

        private int _movingPosition;
        private int _gapPosition;

        @Override
        public void onItemClick(int adapterPosition) {
            if (_state.selected.isEmpty()) {
                TicketActivity.open(MainActivity.this, _tickets.valueAt(adapterPosition).id);
            }
            else {
                _state.selected.flip(adapterPosition);
                if (_state.selected.isEmpty()) {
                    if (_actionMode != null) {
                        _actionMode.finish();
                    }
                    _ticketsTabLayout.listViewFrame().stopMultiSelection();

                    _ticketsAdapter.setEntries(_tickets.map(tickets -> new TicketEntry(tickets.name, false)));
                }
                else {
                    if (_actionMode != null) {
                        _actionMode.setTitle("" + _state.selected.size());
                    }

                    _ticketsAdapter.setEntries(_tickets.indexes().map(p -> new TicketEntry(_tickets.valueAt(p).name, _state.selected.contains(p))));
                }
            }
        }

        @Override
        public void onLongClickStart(int adapterPosition) {
            ensureValidState(_floatingView == null);
            final int firstVisiblePosition = _ticketsTabLayout.listView().getFirstVisiblePosition();
            final View childView = _ticketsTabLayout.listView().getChildAt(adapterPosition - firstVisiblePosition);
            _originalTopPosition = childView.getTop();
            _originalHeight = childView.getHeight();

            _floatingView = _ticketsAdapter.getView(adapterPosition, null, _ticketsTabLayout.listViewFrame());
            _floatingView.findViewById(R.id.textView).setBackgroundColor(0xFF99FFDD);
            final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) _floatingView.getLayoutParams();
            lp.topMargin = _originalTopPosition;
            _floatingView.setLayoutParams(lp);
            _ticketsTabLayout.listViewFrame().addView(_floatingView);
        }

        @Override
        public void onMultiSelectionStart(int adapterPosition) {
            ensureValidState(_floatingView != null);
            _ticketsTabLayout.listViewFrame().removeView(_floatingView);
            _floatingView = null;

            _state.selected.add(adapterPosition);
            _ticketsAdapter.setEntries(_tickets.indexes().map(p -> new TicketEntry(_tickets.valueAt(p).name, p == adapterPosition)));
            startActionMode(new ActionModeCallback());
        }

        private void updateAdapter() {
            _ticketsAdapter.setEntries(_tickets.indexes().map(position -> {
                if (_movingPosition == _gapPosition) {
                    if (position == _gapPosition) {
                        return new TicketEntry("", false);
                    }
                    else {
                        return new TicketEntry(_tickets.valueAt(position).name, false);
                    }
                }
                else if (_movingPosition < _gapPosition) {
                    if (position < _movingPosition) {
                        return new TicketEntry(_tickets.valueAt(position).name, false);
                    }
                    else if (position < _gapPosition) {
                        return new TicketEntry(_tickets.valueAt(position + 1).name, false);
                    }
                    else if (position == _gapPosition) {
                        return new TicketEntry("", false);
                    }
                    else {
                        return new TicketEntry(_tickets.valueAt(position).name, false);
                    }
                }
                else { // _gapPosition < _movingPosition
                    if (position < _gapPosition) {
                        return new TicketEntry(_tickets.valueAt(position).name, false);
                    }
                    else if (position == _gapPosition) {
                        return new TicketEntry("", false);
                    }
                    else if (position <= _movingPosition) {
                        return new TicketEntry(_tickets.valueAt(position - 1).name, false);
                    }
                    else {
                        return new TicketEntry(_tickets.valueAt(position).name, false);
                    }
                }
            }));
        }

        @Override
        public void onSortingStart(int adapterPosition) {
            _movingPosition = adapterPosition;
            _gapPosition = adapterPosition;
            updateAdapter();
        }

        @Override
        public void onSortingMove(int diffY) {
            final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) _floatingView.getLayoutParams();
            lp.topMargin = _originalTopPosition + diffY;
            _floatingView.setLayoutParams(lp);

            final int newGapPosition;
            if (diffY >= 0) {
                newGapPosition = _movingPosition + (diffY + _originalHeight / 2) / _originalHeight;
            }
            else {
                newGapPosition = _movingPosition + (diffY - _originalHeight / 2) / _originalHeight;
            }

            if (newGapPosition != _gapPosition && newGapPosition >= 0 && newGapPosition < _tickets.size()) {
                _gapPosition = newGapPosition;
                updateAdapter();
            }
        }

        @Override
        public void onSortingFinished(int diffY) {
            ensureValidState(_floatingView != null);
            _ticketsTabLayout.listViewFrame().removeView(_floatingView);
            _floatingView = null;

            final TicketsDbManagerImpl manager = DbManager.getInstance().getManager();
            if (!manager.moveTicket(_selectedProjectId, _movingPosition, _gapPosition)) {
                throw new AssertionError();
            }

            // TODO: Avoid calling the database
            final int projectCount = _projects.size();
            if (projectCount == 1) {
                _tickets = manager.getAllTicketReferences();
            }
            else {
                _tickets = manager.getAllTicketReferencesForProject(_selectedProjectId);
            }

            _ticketsAdapter.setEntries(_tickets.indexes().map(p -> new TicketEntry(_tickets.valueAt(p).name, _state.selected.contains(p))));
        }
    }

    private static final class ReleaseFullEntry {
        @NonNull
        final ReleaseId id;
        final int major;
        final int minor;
        final int bugFix;

        ReleaseFullEntry(@NonNull ReleaseId id, int major, int minor, int bugFix) {
            ensureNonNull(id);
            this.id = id;
            this.major = major;
            this.minor = minor;
            this.bugFix = bugFix;
        }
    }

    private static final class State implements Parcelable {
        boolean releasesTabSelected;
        @NonNull
        final MutableBitSet selected;
        boolean displayingDeleteConfirmationDialog;

        State(@NonNull MutableBitSet selected) {
            ensureNonNull(selected);
            this.selected = selected;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeBoolean(releasesTabSelected);

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
            @NonNull
            @Override
            public State createFromParcel(@NonNull Parcel in) {
                final boolean releasesTabSelected = in.readBoolean();
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
                state.releasesTabSelected = releasesTabSelected;
                if (pageCount != 0) {
                    state.displayingDeleteConfirmationDialog = in.readBoolean();
                }

                return state;
            }

            @NonNull
            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };
    }
}
