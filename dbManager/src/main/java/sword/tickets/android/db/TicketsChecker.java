package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.collections.ImmutableMap;
import sword.tickets.android.models.Ticket;

public interface TicketsChecker<ProjectId, TicketId> {
    @NonNull
    ImmutableMap<TicketId, String> getAllTickets();

    @NonNull
    ImmutableMap<ProjectId, String> getAllProjects();
    boolean hasAtLeastOneProject();
    Ticket<ProjectId> getTicket(@NonNull TicketId id);
}
