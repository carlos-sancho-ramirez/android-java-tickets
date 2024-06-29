package sword.tickets.android.models;

import androidx.annotation.NonNull;

import static sword.tickets.android.models.PreconditionUtils.ensureNonNull;

public final class Ticket<ProjectId> {
    public final String name;
    public final String description;

    @NonNull
    public final ProjectId projectId;

    public Ticket(String name, String description, @NonNull ProjectId projectId) {
        ensureNonNull(projectId);
        this.name = name;
        this.description = description;
        this.projectId = projectId;
    }
}
