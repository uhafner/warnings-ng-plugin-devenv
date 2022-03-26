package io.jenkins.plugins.util;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import jenkins.model.Jenkins;

import static org.assertj.core.api.Assertions.*;

/**
 * Verifies the new rule with method scope.
 */
class JenkinsRuleResolverWithJenkinsPerSuiteTest extends IntegrationTestWithJenkinsPerSuite {
    @Test
    void shouldCreateJenkinsPerSuite() throws IOException {
        Jenkins jenkins = getJenkins().jenkins;
        assertThat(jenkins.getJobNames()).isEmpty();

        String job = "job-0";
        getJenkins().createFreeStyleProject(job);

        assertThat(jenkins.getJobNames()).containsExactly(job);
    }
}
