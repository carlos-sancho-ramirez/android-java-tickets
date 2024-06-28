package sword.tickets.android;

import android.content.Context;
import android.content.Intent;

import org.junit.Test;

import sword.tickets.android.activities.MainActivity;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.fail;
import static sword.tickets.android.DbManagerTestUtils.withMemoryDatabase;

public final class TicketManipulationTest {

    private void assertScenarionDestroyed(@NonNull ActivityScenario<MainActivity> scenario) {
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

    @Test
    public void createTicket() {
        withMemoryDatabase(db -> {
            final Context targetContext = ApplicationProvider.getApplicationContext();
            final Intent intent = new Intent(targetContext, MainActivity.class);
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
                Espresso.openActionBarOverflowOrOptionsMenu(targetContext);
                onView(withText(R.string.optionNew)).perform(click());

                onView(withId(R.id.ticketNameField)).perform(scrollTo(), click(), typeText("My new issue"));
                onView(withId(R.id.ticketDescriptionField)).perform(scrollTo(), click(), typeText("This is my new ticket"));
                Espresso.pressBack(); // Closes the keyboard

                onView(withId(R.id.submitButton)).perform(scrollTo(), click());

                onView(withId(R.id.listView)).check(matches(withChild(withChild(withText("My new issue")))));

                Espresso.pressBackUnconditionally(); // Closes the list of tickets
                assertScenarionDestroyed(scenario);
            }
        });
    }

    @Test
    public void editTicket() {
        withMemoryDatabase(db -> {
            DbManager.getInstance().getManager().newTicket("My isue", "Ths is my new ticket");

            final Context targetContext = ApplicationProvider.getApplicationContext();
            final Intent intent = new Intent(targetContext, MainActivity.class);
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
                onView(withText("My isue")).perform(click());

                onView(withId(R.id.ticketNameField)).check(matches(withText("My isue")));
                onView(withId(R.id.ticketDescriptionField)).check(matches(withText("Ths is my new ticket")));

                Espresso.openActionBarOverflowOrOptionsMenu(targetContext);
                onView(withText(R.string.optionEdit)).perform(click());

                onView(withId(R.id.ticketNameField)).perform(scrollTo(), click(), replaceText("My issue"));
                onView(withId(R.id.ticketDescriptionField)).perform(scrollTo(), click(), replaceText("This is my new ticket"));
                Espresso.pressBack(); // Closes the keyboard

                onView(withId(R.id.saveButton)).perform(scrollTo(), click());

                onView(withId(R.id.ticketNameField)).check(matches(withText("My issue")));
                onView(withId(R.id.ticketDescriptionField)).check(matches(withText("This is my new ticket")));
                Espresso.pressBack(); // Closes the ticket details

                onView(withId(R.id.listView)).check(matches(withChild(withChild(withText("My issue")))));

                Espresso.pressBackUnconditionally(); // Closes the list of tickets
                assertScenarionDestroyed(scenario);
            }
        });
    }
}
