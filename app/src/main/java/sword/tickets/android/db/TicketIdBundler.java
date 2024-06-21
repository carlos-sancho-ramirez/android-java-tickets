package sword.tickets.android.db;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

public final class TicketIdBundler {

    public static TicketId read(@NonNull Bundle bundle, @NonNull String key) {
        final int idKey = bundle.getInt(key, 0);
        return (idKey != 0)? new TicketId(idKey) : null;
    }

    public static void write(@NonNull Bundle bundle, @NonNull String key, TicketId ticketId) {
        if (ticketId != null) {
            bundle.putInt(key, ticketId.key);
        }
    }

    public static TicketId readAsIntentExtra(@NonNull Intent intent, @NonNull String key) {
        final int idKey = intent.getIntExtra(key, 0);
        return (idKey != 0)? new TicketId(idKey) : null;
    }

    public static void writeAsIntentExtra(@NonNull Intent intent, @NonNull String key, TicketId ticketId) {
        if (ticketId != null) {
            intent.putExtra(key, ticketId.key);
        }
    }

    private TicketIdBundler() {
    }
}
