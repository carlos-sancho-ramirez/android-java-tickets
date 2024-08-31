package sword.tickets.android.activities;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;

import org.hamcrest.Description;
import org.junit.Test;

import sword.tickets.android.Intentions;
import sword.tickets.android.R;
import sword.tickets.android.db.DbFixtures;
import sword.tickets.android.db.TicketsDbSchema;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static sword.tickets.android.DbManagerTestUtils.withMemoryDatabase;
import static sword.tickets.android.EspressoUtils.assertScenarioDestroyed;

public final class MainActivityTest {

    @NonNull
    private org.hamcrest.Matcher<Intent> isCreateTicketIntention() {
        return new org.hamcrest.Matcher<Intent>() {

            @Override
            public void describeTo(Description description) {
                // TODO: To be implemented
            }

            @Override
            public boolean matches(Object item) {
                if (!(item instanceof Intent)) {
                    return false;
                }

                final Intent intent = (Intent) item;
                final ComponentName component = intent.getComponent();
                return component != null && component.getClassName().endsWith("NewProjectActivity");
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
                // TODO: To be implemented
            }

            @Override
            public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
                // TODO: To be implemented
            }
        };
    }

    @Test
    public void abortNewTicketCreationWhenNoProjectPresent() {
        try {
            Intents.init();
            Intents.intending(isCreateTicketIntention()).respondWith(new Instrumentation.ActivityResult(TicketActivity.RESULT_CANCELED, null));
            withMemoryDatabase(db -> {
                final Context targetContext = ApplicationProvider.getApplicationContext();
                final Intent intent = new Intent(targetContext, MainActivity.class);
                try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
                    onView(withText(R.string.noProjectPlaceholder)).check(matches(isDisplayed()));
                    onView(withId(R.id.newTicketButton)).perform(click());

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    onView(withText(R.string.noProjectPlaceholder)).check(matches(isDisplayed()));
                    onView(withId(R.id.newTicketButton)).check(matches(isDisplayed()));

                    Espresso.pressBackUnconditionally(); // Closes the MainActivity
                    assertScenarioDestroyed(scenario);
                }
            });
        }
        finally {
            Intents.release();
        }
    }

    @Test
    public void newTicketCreationWhenNoProjectPresent() {
        try {
            Intents.init();
            withMemoryDatabase(db -> {
                final Context targetContext = ApplicationProvider.getApplicationContext();

                final String ticketName = "Issue to be solved";
                Intents.intending(isCreateTicketIntention()).respondWithFunction(intent -> {
                    final int projectId = DbFixtures.newProject(db, "My project");
                    final int ticketId = DbFixtures.newTicket(db, ticketName, "This is improtant, fix it as soon as possible!", projectId, 0, TicketsDbSchema.TicketType.ISSUE, 1);

                    final Intent resultIntent = new Intent();
                    resultIntent.putExtra(Intentions.ResultKeys.TICKET_ID, ticketId);
                    return new Instrumentation.ActivityResult(TicketActivity.RESULT_OK, resultIntent);
                });

                final Intent intent = new Intent(targetContext, MainActivity.class);
                try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
                    onView(withText(R.string.noProjectPlaceholder)).check(matches(isDisplayed()));
                    onView(withId(R.id.newTicketButton)).perform(click());

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    onView(withText(R.string.noProjectPlaceholder)).check(doesNotExist());
                    onView(withId(R.id.newTicketButton)).check(doesNotExist());
                    onView(withText(ticketName)).check(matches(isDisplayed()));

                    Espresso.pressBackUnconditionally(); // Closes the list of tickets
                    assertScenarioDestroyed(scenario);
                }
            });
        }
        finally {
            Intents.release();
        }
    }
}
