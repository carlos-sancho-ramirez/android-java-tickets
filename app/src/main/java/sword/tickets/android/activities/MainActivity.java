package sword.tickets.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import sword.collections.ImmutableList;
import sword.tickets.android.DbManager;
import sword.tickets.android.R;
import sword.tickets.android.list.adapters.MainAdapter;

public final class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final ImmutableList<String> tickets = DbManager.getInstance().getManager().getAllTickets().toList();
        this.<ListView>findViewById(R.id.listView).setAdapter(new MainAdapter(tickets));
    }
}
