package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import sword.tickets.android.R;

public final class NewTicketActivity extends android.app.Activity {

    public static void open(@NonNull Activity activity, int requestCode) {
        final Intent intent = new Intent(activity, NewTicketActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_ticket);
    }
}
