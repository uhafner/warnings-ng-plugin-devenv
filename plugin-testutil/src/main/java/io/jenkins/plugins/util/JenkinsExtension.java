package io.jenkins.plugins.util;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * JUnit 5 extension providing {@link JenkinsRule} integration.
 *
 * @see EnableJenkins
 */
class JenkinsExtension implements ParameterResolver, AfterEachCallback {
    private static final String KEY = "jenkins-instance";
    private static final ExtensionContext.Namespace NAMESPACE
            = ExtensionContext.Namespace.create(JenkinsExtension.class);

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        final JenkinsRule rule = context.getStore(NAMESPACE).remove(KEY, JenkinsRule.class);
        if (rule == null) {
            return;
        }
        rule.after();
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(JenkinsRule.class);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
            throws ParameterResolutionException {
        JenkinsRule rule = extensionContext.getStore(NAMESPACE).getOrComputeIfAbsent(KEY, key
                -> new JUnit5JenkinsRule(parameterContext, extensionContext), JenkinsRule.class);

        return invokeRule(rule);
    }

    @SuppressWarnings({"PMD.AvoidCatchingThrowable", "checkstyle:IllegalCatch"})
    private JenkinsRule invokeRule(final JenkinsRule rule) {
        try {
            rule.before();
            return rule;
        }
        catch (Throwable t) {
            throw new ParameterResolutionException(t.getMessage(), t);
        }
    }
}
