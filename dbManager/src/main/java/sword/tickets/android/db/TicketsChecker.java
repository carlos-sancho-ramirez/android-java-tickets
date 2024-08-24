package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.tickets.android.models.Release;
import sword.tickets.android.models.Ticket;
import sword.tickets.android.models.TicketReference;

public interface TicketsChecker<ProjectId, ReleaseId, TicketId> {
    @NonNull
    ImmutableList<TicketReference<TicketId>> getAllTicketReferences();

    @NonNull
    ImmutableList<TicketReference<TicketId>> getAllTicketReferencesForProject(@NonNull ProjectId projectId);

    @NonNull
    ImmutableSet<TicketReference<TicketId>> getAllTicketReferencesWithoutReleaseForProject(@NonNull ProjectId projectId);
    boolean hasAtLeastOneTicketWithoutReleaseForProject(@NonNull ProjectId projectId);

    @NonNull
    ImmutableMap<ProjectId, String> getAllProjects();
    boolean hasAtLeastOneProject();

    @NonNull
    ImmutableMap<ReleaseId, Release> getAllReleasesForProject(@NonNull ProjectId projectId);
    boolean hasAtLeastOneRelease();

    Release getRelease(@NonNull ReleaseId id);
    Ticket<ProjectId, ReleaseId> getTicket(@NonNull TicketId id);
}
