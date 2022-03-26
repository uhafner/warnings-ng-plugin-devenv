package io.jenkins.plugins.util;

import org.junit.jupiter.api.BeforeAll;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Base class for integration tests in Jenkins. Subclasses will get a new and fresh Jenkins instance for each test
 * case.
 *
 * @author Ullrich Hafner
 */
@EnableJenkins
public abstract class IntegrationTestWithJenkinsPerSuite extends IntegrationTest {
    private static JenkinsRule jenkinsPerSuite;

    @BeforeAll
    static void initializeJenkins(final JenkinsRule rule) {
        jenkinsPerSuite = rule;
    }

    @Override
    protected JenkinsRule getJenkins() {
        return jenkinsPerSuite;
    }
}
