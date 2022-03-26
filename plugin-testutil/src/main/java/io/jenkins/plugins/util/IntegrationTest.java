package io.jenkins.plugins.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

import org.junit.jupiter.api.Tag;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.JSONWebResponse;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;

import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.ResourceTest;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.FilePath;
import hudson.Functions;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Slave;
import hudson.model.TopLevelItem;
import hudson.model.labels.LabelAtom;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy;
import hudson.slaves.DumbSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;
import hudson.tasks.BatchFile;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import jenkins.security.s2m.AdminWhitelistRule;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

/**
 * Base class for integration tests in Jenkins.
 *
 * @author Ullrich Hafner
 */
@Tag("IntegrationTest")
@SuppressWarnings({"ClassDataAbstractionCoupling", "ClassFanOutComplexity", "SameParameterValue", "PMD.SystemPrintln", "PMD.GodClass", "PMD.ExcessiveClassLength", "PMD.ExcessiveImports", "PMD.CouplingBetweenObjects", "PMD.CyclomaticComplexity", "unused"})
public abstract class IntegrationTest extends ResourceTest {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final String SSH_AGENT_NAME = "ssh-agent-rsa";
    private static final String SSH_KEY_PATH = "ssh/rsa-key";
    private static final String SSH_KEY_PUB_PATH = "ssh/rsa-key.pub";
    private static final String SSH_SSHD_CONFIG = "ssh/sshd_config";
    private static final String SSH_AUTHORIZED_KEYS = "ssh/authorized_keys";
    private static final String SSH_CREDENTIALS_ID = "sshCredentialsId";
    private static final String DOCKERFILE = "Dockerfile";
    private static final String USER = "jenkins";
    private static final String PASSPHRASE = "";
    private static final int SSH_PORT = 22;
    private static final String AGENT_WORK_DIR = "/home/jenkins";
    private static final String WINDOWS_FILE_ACCESS_READ_ONLY = "RX";
    private static final String WINDOWS_FILE_DENY = "/deny";

    /**
     * Returns the Jenkins rule to manage the Jenkins instance.
     *
     * @return Jenkins rule
     */
    protected abstract JenkinsRule getJenkins();

    /**
     * Creates a file with the specified content in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the files to create
     * @param content
     *         the content of the file
     */
    protected void createFileInWorkspace(final TopLevelItem job, final String fileName, final String content) {
        try {
            FilePath workspace = getWorkspace(job);

            FilePath child = workspace.child(fileName);
            child.copyFrom(new ByteArrayInputStream(content.getBytes(UTF_8)));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Copies the specified files to the workspace. The copied files will have the same file name in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     */
    protected void copyMultipleFilesToWorkspace(final TopLevelItem job, final String... fileNames) {
        copyWorkspaceFiles(job, fileNames, file -> Paths.get(file).getFileName().toString());
    }

    /**
     * Copies the specified file to the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the file to copy
     */
    protected void copySingleFileToWorkspace(final TopLevelItem job, final String fileName) {
        FilePath workspace = getWorkspace(job);

        copySingleFileToWorkspace(workspace, fileName, fileName);
    }

    /**
     * Copies the specified files to the workspace. Uses the specified new file name in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param from
     *         the file to copy
     * @param to
     *         the file name in the workspace
     */
    protected void copySingleFileToWorkspace(final TopLevelItem job, final String from, final String to) {
        FilePath workspace = getWorkspace(job);

        copySingleFileToWorkspace(workspace, from, to);
    }

    /**
     * Copies the specified files to the workspace. The file names of the copied files will be determined by the
     * specified mapper.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     * @param fileNameMapper
     *         maps input file names to output file names
     */
    protected void copyWorkspaceFiles(final TopLevelItem job, final String[] fileNames,
            final Function<String, String> fileNameMapper) {
        Arrays.stream(fileNames)
                .forEach(fileName -> copySingleFileToWorkspace(job, fileName, fileNameMapper.apply(fileName)));
    }

    /**
     * Returns the console log as a String.
     *
     * @param build
     *         the build to get the log for
     *
     * @return the console log
     */
    protected String getConsoleLog(final Run<?, ?> build) {
        try {
            return JenkinsRule.getLog(build);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Copies the specified file to the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the file to copy
     */
    protected void copyFileToWorkspace(final TopLevelItem job, final String fileName) {
        FilePath workspace = getWorkspace(job);

        copyFileToWorkspace(workspace, fileName, fileName);
    }

    /**
     * Copies the specified files to the workspace. Uses the specified new file name in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param from
     *         the file to copy
     * @param to
     *         the file name in the workspace
     */
    protected void copyFileToWorkspace(final TopLevelItem job, final String from, final String to) {
        FilePath workspace = getWorkspace(job);

        copyFileToWorkspace(workspace, from, to);
    }

    /**
     * Copies the specified files to the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     */
    protected void copyFilesToWorkspace(final TopLevelItem job, final String... fileNames) {
        Arrays.stream(fileNames).forEach(fileName -> copyFileToWorkspace(job, fileName, fileName));
    }

    /**
     * Copies the specified directory recursively to the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param directory
     *         the directory to copy
     */
    protected void copyDirectoryToWorkspace(final TopLevelItem job, final String directory) {
        try {
            URL resource = getTestResourceClass().getResource(directory);
            assertThat(resource).as("No such file: %s", directory).isNotNull();
            FilePath destination = new FilePath(new File(resource.getFile()));
            assertThat(destination.exists()).as("Directory %s does not exist", resource.getFile()).isTrue();
            destination.copyRecursiveTo(getWorkspace(job));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private void copyFileToWorkspace(final FilePath workspace, final String from, final String to) {
        try {
            workspace.child(to).copyFrom(asInputStream(from));
            System.out.format("Copying file '%s' as workspace file '%s'%n", from, to);
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the workspace for the specified job.
     *
     * @param job
     *         the job to get the workspace for
     *
     * @return the workspace
     */
    protected FilePath getWorkspace(final TopLevelItem job) {
        FilePath workspace = getJenkins().jenkins.getWorkspaceFor(job);
        assertThat(workspace).isNotNull();
        return workspace;
    }

    /**
     * Returns the absolute path of the specified file.
     *
     * @param job
     *         the job with the workspace that contains the file
     * @param fileName
     *         the file name
     *
     * @return the workspace
     */
    protected String getAbsolutePathOfWorkspaceFile(final TopLevelItem job, final String fileName) {
        return new PathUtil().createAbsolutePath(getWorkspace(job).getRemote(), fileName);
    }

    private void copySingleFileToWorkspace(final FilePath workspace, final String from, final String to) {
        try {
            workspace.child(to).copyFrom(asInputStream(from));
            System.out.format("Copying file '%s' as workspace file '%s'%n (workspace '%s')", from, to, workspace);
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates an {@link DumbSlave agent} with the specified label.
     *
     * @param label
     *         the label of the agent
     *
     * @return the agent
     */
    @SuppressWarnings("illegalcatch")
    protected Slave createAgent(final String label) {
        try {
            return getJenkins().createOnlineSlave(new LabelAtom(label));
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates an {@link Node agent} that runs in the provided docker container.
     *
     * @param agentContainer
     *         the docker container to use as agent
     *
     * @return the agent
     */
    protected Node createDockerAgent(final AgentContainer agentContainer) {
        try {
            Node node = createPermanentAgent(agentContainer.getHost(), agentContainer.getMappedPort(SSH_PORT));
            waitForAgentConnected(node);
            return node;
        }
        catch (FormException | IOException | InterruptedException exception) {
            throw new AssertionError(exception);
        }
    }

    private Node createPermanentAgent(final String host, final int sshPort)
            throws Descriptor.FormException, IOException {
        String privateKey = toString("/" + SSH_KEY_PATH);
        BasicSSHUserPrivateKey.DirectEntryPrivateKeySource privateKeySource
                = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privateKey);
        BasicSSHUserPrivateKey credentials
                = new BasicSSHUserPrivateKey(CredentialsScope.SYSTEM, SSH_CREDENTIALS_ID, USER, privateKeySource,
                PASSPHRASE, "Private Key ssh credentials");
        SystemCredentialsProvider.getInstance().getDomainCredentialsMap().put(Domain.global(),
                Collections.singletonList(credentials));
        SSHLauncher launcher = new SSHLauncher(host, sshPort, SSH_CREDENTIALS_ID);
        launcher.setSshHostKeyVerificationStrategy(new NonVerifyingKeyVerificationStrategy());
        DumbSlave agent = new DumbSlave(SSH_AGENT_NAME, AGENT_WORK_DIR, launcher);
        agent.setNodeProperties(Collections.singletonList(new EnvironmentVariablesNodeProperty(new Entry("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk-amd64"))));
        Jenkins jenkins = getJenkins().jenkins;
        jenkins.addNode(agent);
        return jenkins.getNode(agent.getNodeName());
    }

    @SuppressWarnings("BusyWait")
    private void waitForAgentConnected(final Node node) throws InterruptedException {
        int count = 0;
        while (!Objects.requireNonNull(node.toComputer()).isOnline() && count < 150) {
            Thread.sleep(1000);
            count++;
        }

        assertThat(Objects.requireNonNull(node.toComputer()).isOnline()).isTrue();
    }

    /**
     * Creates an {@link DumbSlave agent} with the specified label. Master - agent security will be enabled.
     *
     * @param label
     *         the label of the agent
     *
     * @return the agent
     */
    protected Slave createAgentWithEnabledSecurity(final String label) {
        try {
            Slave agent = createAgent(label);

            FilePath child = getJenkins().getInstance()
                    .getRootPath()
                    .child("secrets/filepath-filters.d/30-default.conf");
            child.delete();
            child.write("", "ISO_8859_1");

            Objects.requireNonNull(getJenkins().jenkins.getInjector())
                    .getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false);
            getJenkins().jenkins.save();
            return agent;
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the agent workspace of a job.
     *
     * @param agent
     *         the agent
     * @param job
     *         the job
     *
     * @return path to the workspace
     */
    protected FilePath getAgentWorkspace(final Node agent, final TopLevelItem job) {
        FilePath workspace = agent.getWorkspaceFor(job);
        assertThat(workspace).isNotNull();
        return workspace;
    }

    /**
     * Creates the specified file with the given content to the workspace of the specified agent.
     *
     * @param agent
     *         the agent to get the workspace for
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the file name
     * @param content
     *         the content to write
     */
    protected void createFileInAgentWorkspace(final Node agent, final TopLevelItem job, final String fileName,
            final String content) {
        try {
            FilePath workspace = getAgentWorkspace(agent, job);
            FilePath child = workspace.child(fileName);
            child.copyFrom(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Copies the specified files to the workspace of the specified agent. Uses the specified new file name in the
     * workspace.
     *
     * @param agent
     *         the agent to get the workspace for
     * @param job
     *         the job to get the workspace for
     * @param from
     *         the file to copy
     * @param to
     *         the file name in the workspace
     */
    protected void copySingleFileToAgentWorkspace(final Slave agent, final TopLevelItem job,
            final String from, final String to) {
        FilePath workspace = getAgentWorkspace(agent, job);

        copyFileToWorkspace(workspace, from, to);
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job}. The job will get a generated name.
     *
     * @return the created job
     */
    protected FreeStyleProject createFreeStyleProject() {
        return createProject(FreeStyleProject.class);
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job} and copies the specified resources to the workspace folder.
     * The job will get a generated name.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the created job
     */
    protected FreeStyleProject createFreeStyleProjectWithWorkspaceFiles(final String... fileNames) {
        FreeStyleProject job = createFreeStyleProject();
        copyFilesToWorkspace(job, fileNames);
        return job;
    }

    /**
     * Creates a new job of the specified type. The job will get a generated name.
     *
     * @param type
     *         type of the job
     * @param <T>
     *         the project type
     *
     * @return the created job
     */
    protected <T extends TopLevelItem> T createProject(final Class<T> type) {
        try {
            return getJenkins().createProject(type);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new job of the specified type.
     *
     * @param type
     *         type of the job
     * @param name
     *         the name of the job
     * @param <T>
     *         the project type
     *
     * @return the created job
     */
    protected <T extends TopLevelItem> T createProject(final Class<T> type, final String name) {
        try {
            return getJenkins().createProject(type, name);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Wraps the specified steps into a stage.
     *
     * @param steps
     *         the steps of the stage
     *
     * @return the pipeline script
     */
    @SuppressWarnings({"UseOfSystemOutOrSystemErr", "PMD.ConsecutiveLiteralAppends"})
    protected CpsFlowDefinition asStage(final String... steps) {
        StringBuilder script = new StringBuilder(1024);
        script.append("node {\n");
        script.append("  stage ('Integration Test') {\n");
        for (String step : steps) {
            script.append("    ");
            script.append(step);
            script.append('\n');
        }
        script.append("  }\n");
        script.append("}\n");

        String jenkinsFile = script.toString();
        logJenkinsFile(jenkinsFile);
        return new CpsFlowDefinition(jenkinsFile, true);
    }

    /**
     * Creates an empty pipeline job and populates the workspace of that job with copies of the specified files.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the pipeline job
     */
    protected WorkflowJob createPipelineWithWorkspaceFiles(final String... fileNames) {
        WorkflowJob job = createPipeline();
        copyFilesToWorkspace(job, fileNames);
        return job;
    }

    /**
     * Creates an empty pipeline job.
     *
     * @return the pipeline job
     */
    protected WorkflowJob createPipeline() {
        return createProject(WorkflowJob.class);
    }

    /**
     * Creates an empty pipeline job with the specified name.
     *
     * @param name
     *         the name of the job
     *
     * @return the pipeline job
     */
    protected WorkflowJob createPipeline(final String name) {
        return createProject(WorkflowJob.class, name);
    }

    /**
     * Reads a JenkinsFile (i.e. a {@link FlowDefinition}) from the specified file.
     *
     * @param fileName
     *         path to the JenkinsFile
     *
     * @return the JenkinsFile as {@link FlowDefinition} instance
     */
    protected FlowDefinition readJenkinsFile(final String fileName) {
        String script = toString(fileName);
        logJenkinsFile(script);
        return new CpsFlowDefinition(script, true);
    }

    /**
     * Schedules a build for the specified job and waits for the job to finish. After the build has been finished the
     * builds result is checked to be equals to {@link Result#SUCCESS}.
     *
     * @param job
     *         the job to schedule
     *
     * @return the finished build with status {@link Result#SUCCESS}
     */
    protected Run<?, ?> buildSuccessfully(final ParameterizedJob<?, ?> job) {
        return buildWithResult(job, Result.SUCCESS);
    }

    /**
     * Asserts that the builds result is equal to {@link Result#SUCCESS}.
     *
     * @param run
     *         the run to check
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void assertSuccessfulBuild(final Run<?, ?> run) {
        try {
            getJenkins().assertBuildStatus(Result.SUCCESS, run);
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    /**
     * Schedules a build for the specified job and waits for the job to finish. After the build has been finished the
     * builds result is checked to be equals to {@code expectedResult}.
     *
     * @param job
     *         the job to schedule
     * @param expectedResult
     *         the expected result for the build
     *
     * @return the finished build with status {@code expectedResult}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    protected Run<?, ?> buildWithResult(final ParameterizedJob<?, ?> job, final Result expectedResult) {
        try {
            return getJenkins().assertBuildStatus(expectedResult,
                    Objects.requireNonNull(job.scheduleBuild2(0, new Action[0])));
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Prints the content of the JenkinsFile to StdOut.
     *
     * @param script
     *         the script
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private void logJenkinsFile(final String script) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(script);
        System.out.println("----------------------------------------------------------------------");
    }

    /**
     * Prints the console log of the specified build to StdOut.
     *
     * @param build
     *         the build
     */
    protected void printConsoleLog(final Run<?, ?> build) {
        System.out.println("----- Console Log -----");
        try (Reader reader = build.getLogReader()) {
            try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                bufferedReader.lines().forEach(System.out::println);
            }
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    /**
     * Makes the specified file unreadable.
     *
     * @param file
     *         the specified file
     */
    protected void makeFileUnreadable(final Path file) {
        makeFileUnreadable(file.toString());
    }

    /**
     * Makes the specified file unreadable.
     *
     * @param absolutePath
     *         the specified file
     */
    protected void makeFileUnreadable(final String absolutePath) {
        File nonReadableFile = new File(absolutePath);
        if (Functions.isWindows()) {
            setAccessModeOnWindows(absolutePath, WINDOWS_FILE_DENY, WINDOWS_FILE_ACCESS_READ_ONLY);
        }
        else {
            assertThat(nonReadableFile.setReadable(false, false)).isTrue();
            assumeThat(nonReadableFile.canRead())
                    .as("File ´%s´ could not be made unreadable (OS configuration problem?)", absolutePath)
                    .isFalse();
        }
    }

    private void setAccessModeOnWindows(final String path, final String command, final String accessMode) {
        try {
            Process process = Runtime.getRuntime()
                    .exec("icacls \"" + path + "\" " + command + " *S-1-1-0:" + accessMode);
            process.waitFor();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Adds a script as a {@link Shell} or {@link BatchFile} depending on the current OS.
     *
     * @param project
     *         the project
     * @param script
     *         the script to run
     *
     * @return the created script step
     */
    protected Builder addScriptStep(final FreeStyleProject project, final String script) {
        Builder item;
        if (Functions.isWindows()) {
            item = new BatchFile(script);
        }
        else {
            item = new Shell(script);
        }
        project.getBuildersList().add(item);
        return item;
    }

    /**
     * Cleans the workspace of the specified job. Deletes all files in the workspace.
     *
     * @param job
     *         the workspace to clean
     */
    protected void cleanWorkspace(final TopLevelItem job) {
        try {
            getWorkspace(job).deleteContents();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Add a build step that simply fails the build.
     *
     * @param project
     *         the job to add the step
     *
     * @return the created build step
     */
    protected Builder addFailureStep(final FreeStyleProject project) {
        return addScriptStep(project, "exit 1");
    }

    /**
     * Removes the specified builder from the list of registered builders.
     *
     * @param project
     *         the job to add the step
     * @param builder
     *         the builder to remove
     */
    protected void removeBuilder(final FreeStyleProject project, final Builder builder) {
        project.getBuildersList().remove(builder);
    }

    /**
     * Joins the specified arguments as list of comma separated values. Note that the first element is separated with a
     * comma as well.
     * <blockquote>For example,
     * <pre>{@code
     *     String message = join("Java", "is", "cool");
     *     // message returned is: ",Java,is,cool"
     * }</pre></blockquote>
     *
     * @param arguments
     *         th arguments to join
     *
     * @return the concatenated string
     */
    protected String join(final String... arguments) {
        StringBuilder builder = new StringBuilder();
        for (String argument : arguments) {
            builder.append(", ");
            builder.append(argument);
        }
        return builder.toString();
    }

    /**
     * Calls Jenkins remote API with the specified URL. Calls the JSON format.
     *
     * @param url
     *         the URL to call
     *
     * @return the JSON response
     */
    protected JSONWebResponse callJsonRemoteApi(final String url) {
        try {
            return getJenkins().getJSON(url);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Calls Jenkins remote API with the specified URL. Calls the XML format.
     *
     * @param url
     *         the URL to call
     *
     * @return the XML response
     */
    protected Document callXmlRemoteApi(final String url) {
        try {
            try (WebClient webClient = getJenkins().createWebClient()) {
                return webClient.goToXml(url).getXmlDocument();
            }
        }
        catch (IOException | SAXException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Docker container to be used as Jenkins build agent. Provides tools like make, gcc, java 11, or maven.
     */
    public static class AgentContainer extends GenericContainer<AgentContainer> {
        /**
         * Creates a new container that exposes port {@link #SSH_PORT} for SSH connections.
         */
        public AgentContainer() {
            super(new ImageFromDockerfile(SSH_AGENT_NAME, false)
                    .withFileFromClasspath(SSH_AUTHORIZED_KEYS, "/" + SSH_AUTHORIZED_KEYS)
                    .withFileFromClasspath(SSH_KEY_PATH, "/" + SSH_KEY_PATH)
                    .withFileFromClasspath(SSH_KEY_PUB_PATH, "/" + SSH_KEY_PUB_PATH)
                    .withFileFromClasspath(SSH_SSHD_CONFIG, "/" + SSH_SSHD_CONFIG)
                    .withFileFromClasspath(DOCKERFILE, "/ssh/" + DOCKERFILE));

            setExposedPorts(Collections.singletonList(SSH_PORT));
        }
    }
}
