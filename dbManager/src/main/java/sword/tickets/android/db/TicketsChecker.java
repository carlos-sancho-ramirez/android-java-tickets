package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.tickets.android.models.Ticket;
import sword.tickets.android.models.TicketReference;

public interface TicketsChecker<ProjectId, TicketId> {
    @NonNull
    ImmutableList<TicketReference<TicketId>> getAllTickets();

    @NonNull
    ImmutableList<TicketReference<TicketId>> getAllTicketsForProject(@NonNull ProjectId projectId);

    @NonNull
    ImmutableMap<ProjectId, String> getAllProjects();
    boolean hasAtLeastOneProject();
    Ticket<ProjectId> getTicket(@NonNull TicketId id);
}
