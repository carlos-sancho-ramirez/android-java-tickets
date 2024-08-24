package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.tickets.android.db.TicketsDbSchema.TicketType;
import sword.tickets.android.models.Ticket;

public interface TicketsManager<ProjectId, ReleaseId, TicketId> extends TicketsChecker<ProjectId, ReleaseId, TicketId> {
    @NonNull
    ProjectId newProject(String name);

    @NonNull
    ReleaseId newRelease(@NonNull ProjectId project, int major, int minor, int bugFix);

    @NonNull
    TicketId newTicket(String name, String description, @NonNull ProjectId projectId, @NonNull TicketType type);
    boolean updateTicket(@NonNull TicketId ticketId, @NonNull Ticket<ProjectId, ReleaseId> ticket);
    boolean updateTicketRelease(@NonNull TicketId ticketId, ReleaseId newReleaseId);
    boolean deleteTicket(@NonNull TicketId ticketId);
    boolean moveTicket(@NonNull ProjectId projectId, int movingPosition, int gapPosition);
}
