package sword.tickets.android.models;

import sword.tickets.android.db.TicketsDbSchema.TicketState;

import androidx.annotation.NonNull;

import static sword.tickets.android.models.PreconditionUtils.ensureNonNull;

public final class Ticket<ProjectId> {
    public final String name;
    public final String description;

    @NonNull
    public final ProjectId projectId;

    @NonNull
    public final TicketState state;

    public Ticket(String name, String description, @NonNull ProjectId projectId, @NonNull TicketState state) {
        ensureNonNull(projectId, state);
        this.name = name;
        this.description = description;
        this.projectId = projectId;
        this.state = state;
    }
}
