package io.jenkins.plugins.util;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;

import jenkins.model.Jenkins;

import static org.assertj.core.api.Assertions.*;

/**
 * Verifies the new rule with method scope.
 */
class JenkinsRuleResolverWithMethodScopeTest {
    @EnableJenkins
    @Test
    void jenkinsRuleIsAccessible(final JenkinsRule rule) throws IOException {
        Jenkins jenkins = rule.getInstance();
        assertThat(jenkins.getJobNames()).isEmpty();

        String name = "job-0";
        rule.createFreeStyleProject(name);
        assertThat(jenkins.getJobNames()).containsExactly(name);
    }
}
