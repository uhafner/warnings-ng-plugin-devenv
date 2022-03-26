package io.jenkins.plugins.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget.ConstructorCallTarget;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.verb.POST;
import hudson.model.Descriptor;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Defines several architecture rules that should be enforced for every Jenkins plugin.
 *
 * @author Ullrich Hafner
 */
public final class PluginArchitectureRules {
    /** Some packages that are transitive dependencies of Jenkins should not be used at all. */
    public static final ArchRule NO_FORBIDDEN_PACKAGE_ACCESSED
            = noClasses().should().dependOnClassesThat(resideInAnyPackage(
            "org.apache.commons.lang..",
            "org.joda.time..",
            "javax.xml.bind..",
            "net.jcip.annotations..",
            "javax.annotation..",
            "junit..",
            "org.hamcrest..",
            "com.google.common..",
            "org.junit"));

    /**
     * Direct calls to {@link Jenkins#getInstance()} or {@link Jenkins#getInstanceOrNull()}} are prohibited since these
     * methods require a running Jenkins instance. Otherwise the accessor of this method cannot be unit tested. Create a
     * new {@link JenkinsFacade} object to access the running Jenkins instance. If your required method is missing you
     * need to add it to {@link JenkinsFacade}.
     */
    public static final ArchRule NO_JENKINS_INSTANCE_CALL =
            noClasses().that().doNotHaveSimpleName("JenkinsFacade")
                    .should().callMethod(Jenkins.class, "getInstance")
                    .orShould().callMethod(Jenkins.class, "getInstanceOrNull")
                    .orShould().callMethod(Jenkins.class, "getActiveInstance")
                    .orShould().callMethod(Jenkins.class, "get");

    /**
     * Methods that are used as AJAX end points must be in public classes.
     */
    public static final ArchRule AJAX_PROXY_METHOD_MUST_BE_IN_PUBLIC_CLASS =
            methods().that().areAnnotatedWith(JavaScriptMethod.class)
                    .should().bePublic()
                    .andShould().beDeclaredInClassesThat().arePublic().allowEmptyShould(true);

    /**
     * Methods that use data binding must be in public classes.
     */
    public static final ArchRule DATA_BOUND_CONSTRUCTOR_MUST_BE_IN_PUBLIC_CLASS =
            constructors().that().areAnnotatedWith(DataBoundConstructor.class)
                    .should().beDeclaredInClassesThat().arePublic().allowEmptyShould(true);

    /**
     * Methods that use data binding must be in public classes.
     */
    public static final ArchRule DATA_BOUND_SETTER_MUST_BE_IN_PUBLIC_CLASS =
            methods().that().areAnnotatedWith(DataBoundSetter.class)
                    .should().beDeclaredInClassesThat().arePublic().allowEmptyShould(true);

    /**
     * Methods that are used as AJAX end points must be in public classes.
     */
    public static final ArchRule USE_POST_FOR_VALIDATION_END_POINTS =
            methods().that().areDeclaredInClassesThat().areAssignableTo(Descriptor.class)
                    .and().haveNameMatching("doCheck[A-Z].*")
                    .and().haveRawReturnType(FormValidation.class)
                    .should().beAnnotatedWith(POST.class)
                    .andShould().bePublic()
                    .andShould(checkPermissions()).allowEmptyShould(true);

    /**
     * List model methods that are used as AJAX end points must use @POST and have a permission check.
     */
    public static final ArchRule USE_POST_FOR_LIST_AND_COMBOBOX_FILL =
            methods().that().areDeclaredInClassesThat().areAssignableTo(Descriptor.class)
                    .and().haveNameMatching("doFill[A-Z].*")
                    .and().haveRawReturnType(ofAllowedClasses(ComboBoxModel.class, ListBoxModel.class))
                    .should().beAnnotatedWith(POST.class)
                    .andShould().bePublic()
                    .andShould(checkPermissions()).allowEmptyShould(true);

    private static HavePermissionCheck checkPermissions() {
        return new HavePermissionCheck();
    }

    private static DescribedPredicate<JavaClass> ofAllowedClasses(final Class<?>... classes) {
        return new AllowedClasses(classes);
    }

    private PluginArchitectureRules() {
        // prevents instantiation
    }

    /**
     * Matches if a call from outside the defining class uses a method or constructor annotated with {@link
     * VisibleForTesting}. There are two exceptions:
     * <ul>
     * <li>The method is called on the same class</li>
     * <li>The method is called in a method also annotated with {@link VisibleForTesting}</li>
     * </ul>
     */
    private static class AccessRestrictedToTests extends DescribedPredicate<JavaCall<?>> {
        AccessRestrictedToTests() {
            super("access is restricted to tests");
        }

        @Override
        public boolean apply(final JavaCall<?> input) {
            return isVisibleForTesting(input.getTarget())
                    && !input.getOriginOwner().equals(input.getTargetOwner())
                    && !isVisibleForTesting(input.getOrigin());
        }

        private boolean isVisibleForTesting(final CanBeAnnotated target) {
            return target.isAnnotatedWith(VisibleForTesting.class);
        }
    }

    private static class HavePermissionCheck extends ArchCondition<JavaMethod> {
        HavePermissionCheck() {
            super("should have a permission check");
        }

        @Override
        public void check(final JavaMethod item, final ConditionEvents events) {
            if (item.getModifiers().contains(JavaModifier.SYNTHETIC)) {
                return;
            }

            if (item.getCallsFromSelf().stream().anyMatch(
                    javaCall -> javaCall.getTarget().getOwner().getFullName().equals(JenkinsFacade.class.getName())
                            && "hasPermission".equals(javaCall.getTarget().getName()))) {
                return;
            }
            events.add(SimpleConditionEvent.violated(item,
                    String.format("JenkinsFacade.hasPermission() not called in %s in %s",
                            item.getDescription(), item.getSourceCodeLocation())));
        }
    }

    private static class AllowedClasses extends DescribedPredicate<JavaClass> {
        private final List<String> allowedClassNames;

        AllowedClasses(final Class<?>... classes) {
            super("raw return type of any of %s", Arrays.toString(classes));

            allowedClassNames = Arrays.stream(classes).map(Class::getName).collect(Collectors.toList());
        }

        @Override
        public boolean apply(final JavaClass input) {
            return allowedClassNames.contains(input.getFullName());
        }
    }

    private static class ExceptionHasNoContext extends DescribedPredicate<JavaConstructorCall> {
        ExceptionHasNoContext() {
            super("exception context is missing");
        }

        @Override
        public boolean apply(final JavaConstructorCall javaConstructorCall) {
            ConstructorCallTarget target = javaConstructorCall.getTarget();
            if (target.getRawParameterTypes().size() > 0) {
                return false;
            }
            return target.getOwner().isAssignableTo(Throwable.class);
        }
    }

    /**
     * Matches if a code unit of one of the registered classes has been called.
     */
    private static class TargetIsForbiddenClass extends DescribedPredicate<JavaCall<?>> {
        private final String[] classes;

        TargetIsForbiddenClass(final String... classes) {
            super("forbidden class");

            this.classes = Arrays.copyOf(classes, classes.length);
        }

        @Override
        public boolean apply(final JavaCall<?> input) {
            return StringUtils.containsAny(input.getTargetOwner().getFullName(), classes)
                    && !"assertTimeoutPreemptively".equals(input.getName());
        }
    }
}
