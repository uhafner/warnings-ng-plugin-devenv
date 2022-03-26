package io.jenkins.plugins.util;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.assertj.core.api.Assertions.*;

/**
 * Verifies the new rule with method scope.
 */
@EnableJenkins
class JenkinsRuleResolverTest {
    @Test
    void jenkinsRuleIsAccessible(final JenkinsRule rule) throws IOException {
        assertThat(rule.jenkins.getJobNames()).isEmpty();

        String job = "job-0";
        rule.createFreeStyleProject(job);

        assertThat(rule.jenkins.getJobNames()).containsExactly(job);
    }
}
