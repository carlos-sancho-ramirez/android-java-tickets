package sword.tickets.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.tickets.android.DbManager;
import sword.tickets.android.R;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.layout.MainLayoutForActivity;
import sword.tickets.android.list.adapters.MainAdapter;

public final class MainActivity extends Activity {

    private static final int REQUEST_CODE_NEW_TICKET = 1;

    private MainLayoutForActivity _layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _layout = MainLayoutForActivity.attach(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final ImmutableMap<TicketId, String> tickets = DbManager.getInstance().getManager().getAllTickets();
        final ListView listView = _layout.listView();
        listView.setAdapter(new MainAdapter(tickets.toList()));
        listView.setOnItemClickListener((parent, view, position, id) ->
                TicketActivity.open(this, tickets.keyAt(position)));
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
            NewTicketActivity.open(this, REQUEST_CODE_NEW_TICKET);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
