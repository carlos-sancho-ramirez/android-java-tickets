package sword.tickets.android;

import android.content.Context;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;

import sword.tickets.android.activities.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.fail;

public final class EspressoUtils {

    static void clickIfRoomMenuItem(@IdRes int id, @StringRes int text) {
        try {
            onView(withId(id)).perform(click());
        }
        catch (NoMatchingViewException e) {
            final Context targetContext = ApplicationProvider.getApplicationContext();
            openActionBarOverflowOrOptionsMenu(targetContext);
            onView(withText(text)).perform(click());
        }
    }

    public static void assertScenarioDestroyed(@NonNull ActivityScenario<MainActivity> scenario) {
        int retrials = 15;
        while (scenario.getState() != Lifecycle.State.DESTROYED) {
            if (--retrials >= 0) {
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                fail("Scenario is not reaching the DESTROYED state");
            }
        }
    }

    private EspressoUtils() {
    }
}
