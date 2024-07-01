package sword.tickets.android;

import android.content.Context;
import android.content.Intent;

import org.junit.Test;

import sword.database.Database;
import sword.database.DbInsertQuery;
import sword.tickets.android.activities.MainActivity;
import sword.tickets.android.db.TicketsDbSchema;
import sword.tickets.android.db.TicketsDbSchema.Tables;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.fail;
import static sword.tickets.android.DbManagerTestUtils.withMemoryDatabase;
import static sword.tickets.android.db.DbFixtures.newProject;
import static sword.tickets.android.db.DbFixtures.newTicket;

public final class TicketManipulationTest {

    private void assertScenarioDestroyed(@NonNull ActivityScenario<MainActivity> scenario) {
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
    public void createFirstTicket() {
        withMemoryDatabase(db -> {
            final Context targetContext = ApplicationProvider.getApplicationContext();
            final Intent intent = new Intent(targetContext, MainActivity.class);
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
                openActionBarOverflowOrOptionsMenu(targetContext);
                onView(withText(R.string.optionNew)).perform(click());

                onView(withId(R.id.projectNameField)).perform(scrollTo(), click(), typeText("My project"));
                Espresso.pressBack(); // Closes the keyboard

                // Required in some devices to effectively click on the button
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                onView(withId(R.id.nextButton)).perform(scrollTo(), click());

                onView(withId(R.id.ticketNameField)).perform(scrollTo(), click(), typeText("My new issue"));
                onView(withId(R.id.ticketDescriptionField)).perform(scrollTo(), click(), typeText("This is my new ticket"));
                Espresso.pressBack(); // Closes the keyboard

                // Required in some devices to effectively click on the button
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                onView(withId(R.id.submitButton)).perform(scrollTo(), click());
                onView(withId(R.id.listView)).check(matches(withChild(withChild(withText("My new issue")))));

                Espresso.pressBackUnconditionally(); // Closes the list of tickets
                assertScenarioDestroyed(scenario);
            }
        });
    }

    @Test
    public void createTicketForANewProjectWhenAProjectExists() {
        withMemoryDatabase(db -> {
            final int projectId = newProject(db, "My project");
            newTicket(db, "My important issue", "This is an issue that must be solved", projectId);

            final Context targetContext = ApplicationProvider.getApplicationContext();
            final Intent intent = new Intent(targetContext, MainActivity.class);
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
                onView(withId(R.id.listView)).check(matches(withChild(withChild(withText("My important issue")))));

                openActionBarOverflowOrOptionsMenu(targetContext);
                onView(withText(R.string.optionNew)).perform(click());
                onView(withText("My project")).check(matches(isDisplayed()));

                openActionBarOverflowOrOptionsMenu(targetContext);
                onView(withText(R.string.optionNew)).perform(click());

                onView(withId(R.id.projectNameField)).perform(scrollTo(), click(), typeText("My new project"));
                Espresso.pressBack(); // Closes the keyboard

                // Required in some devices to effectively click on the button
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                onView(withId(R.id.nextButton)).perform(scrollTo(), click());

                onView(withId(R.id.ticketNameField)).perform(scrollTo(), click(), typeText("My new issue"));
                onView(withId(R.id.ticketDescriptionField)).perform(scrollTo(), click(), typeText("This is my new ticket"));
                Espresso.pressBack(); // Closes the keyboard

                // Required in some devices to effectively click on the button
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                onView(withId(R.id.submitButton)).perform(scrollTo(), click());
                onView(withId(R.id.listView)).check(matches(withChild(withChild(withText("My important issue")))));
                onView(withId(R.id.listView)).check(matches(withChild(withChild(withText("My new issue")))));

                Espresso.pressBackUnconditionally(); // Closes the list of tickets
                assertScenarioDestroyed(scenario);
            }
        });
    }

    @Test
    public void createTicketForAProjectThatAlreadyExists() {
        withMemoryDatabase(db -> {
            final int projectId = newProject(db, "My project");
            newTicket(db, "My important issue", "This is an issue that must be solved", projectId);

            final Context targetContext = ApplicationProvider.getApplicationContext();
            final Intent intent = new Intent(targetContext, MainActivity.class);
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
                onView(withId(R.id.listView)).check(matches(withChild(withChild(withText("My important issue")))));

                openActionBarOverflowOrOptionsMenu(targetContext);
                onView(withText(R.string.optionNew)).perform(click());
                onView(withText("My project")).perform(click());

                onView(withId(R.id.ticketNameField)).perform(scrollTo(), click(), typeText("My new issue"));
                onView(withId(R.id.ticketDescriptionField)).perform(scrollTo(), click(), typeText("This is my new ticket"));
                Espresso.pressBack(); // Closes the keyboard

                // Required in some devices to effectively click on the button
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                onView(withId(R.id.submitButton)).perform(scrollTo(), click());
                onView(withId(R.id.listView)).check(matches(withChild(withChild(withText("My important issue")))));
                onView(withId(R.id.listView)).check(matches(withChild(withChild(withText("My new issue")))));

                Espresso.pressBackUnconditionally(); // Closes the list of tickets
                assertScenarioDestroyed(scenario);
            }
        });
    }

    @Test
    public void editTicket() {
        withMemoryDatabase(db -> {
            final int projectId = newProject(db, "My project");
            newTicket(db, "My isue", "Ths is my new ticket", projectId);

            final Context targetContext = ApplicationProvider.getApplicationContext();
            final Intent intent = new Intent(targetContext, MainActivity.class);
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
                onView(withText("My isue")).perform(click());

                onView(withId(R.id.ticketNameField)).check(matches(withText("My isue")));
                onView(withId(R.id.ticketDescriptionField)).check(matches(withText("Ths is my new ticket")));

                openActionBarOverflowOrOptionsMenu(targetContext);
                onView(withText(R.string.optionEdit)).perform(click());

                onView(withId(R.id.ticketNameField)).perform(scrollTo(), click(), replaceText("My issue"));
                onView(withId(R.id.ticketDescriptionField)).perform(scrollTo(), click(), replaceText("This is my new ticket"));
                Espresso.pressBack(); // Closes the keyboard

                // Required in some devices to effectively click on the button
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                onView(withId(R.id.saveButton)).perform(scrollTo(), click());

                onView(withId(R.id.ticketNameField)).check(matches(withText("My issue")));
                onView(withId(R.id.ticketDescriptionField)).check(matches(withText("This is my new ticket")));
                Espresso.pressBack(); // Closes the ticket details

                onView(withId(R.id.listView)).check(matches(withChild(withChild(withText("My issue")))));

                Espresso.pressBackUnconditionally(); // Closes the list of tickets
                assertScenarioDestroyed(scenario);
            }
        });
    }

    @Test
    public void deleteTicket() {
        withMemoryDatabase(db -> {
            final int projectId = newProject(db, "My project");
            newTicket(db, "My issue", "This is my new ticket", projectId);

            final Context targetContext = ApplicationProvider.getApplicationContext();
            final Intent intent = new Intent(targetContext, MainActivity.class);
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
                onView(withText("My issue")).perform(longClick());

                onView(withId(R.id.optionDelete)).perform(click());

                onView(withText(targetContext.getString(R.string.deleteTicketConfirmationDialogMessage, 1))).check(matches(isDisplayed()));
                onView(withText(R.string.optionDelete)).perform(click());

                onView(withId(R.id.listView)).check(matches(hasChildCount(0)));

                Espresso.pressBackUnconditionally(); // Closes the list of tickets
                assertScenarioDestroyed(scenario);
            }
        });
    }
}
