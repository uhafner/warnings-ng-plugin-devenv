package io.jenkins.plugins.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import hudson.FilePath;

import static org.mockito.Mockito.*;

/**
 * Several test utilities to create stubs of {@link FilePath} instances.
 *
 * @author Ullrich Hafner
 */
public final class PathStubs {
    /**
     * Creates a collection of paths.
     *
     * @param directories
     *         the directories as an array
     *
     * @return the directories as an collection
     */
    public static Collection<FilePath> asSourceDirectories(final FilePath... directories) {
        return Arrays.asList(directories);
    }

    /**
     * Creates a workspace path stub.
     *
     * @param path
     *         the path of the workspace
     *
     * @return the stub
     */
    public static FilePath createWorkspace(final String path) {
        File file = mock(File.class);
        when(file.getPath()).thenReturn(path);
        return new FilePath(file);
    }

    private PathStubs() {
        // prevents instantiation
    }
}
