package sword.tickets.android.db;

import androidx.annotation.NonNull;
import sword.tickets.android.models.Ticket;

public interface TicketsManager<TicketId> extends TicketsChecker<TicketId> {
    void newTicket(String name, String description);
    boolean updateTicket(@NonNull TicketId ticketId, @NonNull Ticket ticket);
    boolean deleteTicket(@NonNull TicketId ticketId);
}
