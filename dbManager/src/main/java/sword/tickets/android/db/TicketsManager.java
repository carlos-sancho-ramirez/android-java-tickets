package sword.tickets.android.db;

public interface TicketsManager<TicketId> extends TicketsChecker<TicketId> {
    void newTicket(String name, String description);
}