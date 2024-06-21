package sword.tickets.android.db.models;

public final class Ticket {
    public final String name;
    public final String description;

    public Ticket(String name, String description) {
        this.name = name;
        this.description = description;
    }
}