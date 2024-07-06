package sword.tickets.android.models;

import androidx.annotation.NonNull;

import static sword.tickets.android.models.PreconditionUtils.ensureNonNull;

public final class TicketReference<TicketId> {
    @NonNull
    public final TicketId id;
    public final String name;

    public TicketReference(@NonNull TicketId id, String name) {
        ensureNonNull(id);
        this.id = id;
        this.name = name;
    }
}
