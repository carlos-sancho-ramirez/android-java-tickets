package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.tickets.android.db.TicketsDbSchema.TicketType;
import sword.tickets.android.models.Ticket;

public interface TicketsManager<ProjectId, TicketId> extends TicketsChecker<ProjectId, TicketId> {
    @NonNull
    ProjectId newProject(String name);

    @NonNull
    TicketId newTicket(String name, String description, @NonNull ProjectId projectId, @NonNull TicketType type);
    boolean updateTicket(@NonNull TicketId ticketId, @NonNull Ticket<ProjectId> ticket);
    boolean deleteTicket(@NonNull TicketId ticketId);
}
