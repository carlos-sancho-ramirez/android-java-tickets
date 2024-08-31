package sword.tickets.android;

import android.content.Context;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

final class EspressoUtils {

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

    private EspressoUtils() {
    }
}
