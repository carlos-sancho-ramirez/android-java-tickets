package sword.tickets.android.db;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.tickets.android.models.Ticket;
import sword.tickets.android.models.TicketReference;

import androidx.annotation.NonNull;

public interface TicketsChecker<ProjectId, TicketId> {
    @NonNull
    ImmutableList<TicketReference<TicketId>> getAllTickets();

    @NonNull
    ImmutableList<TicketReference<TicketId>> getAllTicketsForProject(@NonNull ProjectId projectId);

    @NonNull
    ImmutableSet<TicketReference<TicketId>> getAllCompletedTicketsForProject(@NonNull ProjectId projectId);

    @NonNull
    ImmutableMap<ProjectId, String> getAllProjects();
    boolean hasAtLeastOneProject();
    Ticket<ProjectId> getTicket(@NonNull TicketId id);
}
