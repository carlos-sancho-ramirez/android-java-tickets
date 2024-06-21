package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.collections.ImmutableMap;
import sword.tickets.android.db.models.Ticket;

public interface TicketsChecker<TicketId> {
    @NonNull
    ImmutableMap<TicketId, String> getAllTickets();
    Ticket getTicket(@NonNull TicketId id);
}
