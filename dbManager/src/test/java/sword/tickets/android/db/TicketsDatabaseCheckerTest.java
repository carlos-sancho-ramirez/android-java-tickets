package sword.tickets.android.db;

import org.junit.jupiter.api.Test;

import sword.database.MemoryDatabase;

import static sword.collections.SizableTestUtils.assertEmpty;

public final class TicketsDatabaseCheckerTest {
    @Test
    void getAllTickets_whenEmpty() {
        final MemoryDatabase db = new MemoryDatabase();
        final TicketsDatabaseChecker<ProjectId, ReleaseId, TicketId> checker = new TicketsDatabaseChecker<>(db, new ProjectIdManager(), new ReleaseIdManager(), new TicketIdManager());
        assertEmpty(checker.getAllTicketReferences());
    }
}
