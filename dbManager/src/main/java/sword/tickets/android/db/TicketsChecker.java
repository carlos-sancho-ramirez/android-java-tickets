package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.collections.ImmutableIntKeyMap;
import sword.collections.ImmutableMap;

public interface TicketsChecker<TicketId> {
    @NonNull
    ImmutableMap<TicketId, String> getAllTickets();
}
