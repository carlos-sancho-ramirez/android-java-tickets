package sword.tickets.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.tickets.android.DbManager;
import sword.tickets.android.R;
import sword.tickets.android.list.adapters.MainAdapter;

public final class MainActivity extends Activity {

    private static final int REQUEST_CODE_NEW_TICKET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final ImmutableList<String> tickets = DbManager.getInstance().getManager().getAllTickets().toList();
        this.<ListView>findViewById(R.id.listView).setAdapter(new MainAdapter(tickets));
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
