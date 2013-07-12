package org.apache.maven.scm.provider.git.jgit.command.diff;

import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.command.diff.GitDiffCommandTckTest;

public class JGitDiffCommandTckTest extends GitDiffCommandTckTest {

    /**
     * {@inheritDoc}
     */
    public String getScmUrl()
        throws Exception
    {
        return GitScmTestUtils.getScmUrl( getRepositoryRoot(), "jgit" );
    }
}
