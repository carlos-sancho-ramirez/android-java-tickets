package sword.tickets.android.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a field that is expected to be changed only for test
 * purposes, and never modified in production code.
 * <p>
 * In order to make tests smaller and avoid testing more than required, tests
 * can modify this field value to avoid creating the real foreign component and
 * inject mock instances instead.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface TestSwitcher {
}
