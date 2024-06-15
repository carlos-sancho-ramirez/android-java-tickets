package sword.tickets.android;

import android.app.Application;

public final class TicketsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DbManager.createInstance(this);
    }
}
