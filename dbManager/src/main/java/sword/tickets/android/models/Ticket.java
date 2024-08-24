package sword.tickets.android.models;

import androidx.annotation.NonNull;

import sword.tickets.android.db.TicketsDbSchema.TicketState;

import static sword.tickets.android.models.PreconditionUtils.ensureNonNull;

public final class Ticket<ProjectId, ReleaseId> {
    public final String name;
    public final String description;

    @NonNull
    public final ProjectId projectId;
    public final ReleaseId releaseId;

    @NonNull
    public final TicketState state;

    public Ticket(String name, String description, @NonNull ProjectId projectId, ReleaseId releaseId, @NonNull TicketState state) {
        ensureNonNull(projectId, state);
        this.name = name;
        this.description = description;
        this.projectId = projectId;
        this.releaseId = releaseId;
        this.state = state;
    }
}
