package org.apache.maven.scm.provider.git.jgit.command.remoteinfo;

import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.command.remoteinfo.AbstractGitRemoteInfoCommandTckTest;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;

/**
 * 
 * @author Dominik Bartholdi (imod)
 */
public class JGitRemoteInfoCommandTckTest extends AbstractGitRemoteInfoCommandTckTest
{

    @Override
    protected void checkResult( RemoteInfoScmResult result )
    {
        assertEquals( 1, result.getBranches().size() );
        assertEquals( "92f139dfec4d1dfb79c3cd2f94e83bf13129668b", result.getBranches().get( "master" ) );

        assertEquals( 0, result.getTags().size() );
    }

    /**
     * {@inheritDoc}
     */
    public String getScmUrl()
        throws Exception
    {
    	String scmUrl = GitScmTestUtils.getScmUrl( getRepositoryRoot(), "jgit" );
    	System.out.println("SCM: "+scmUrl);
        return scmUrl;
    }

    @Override
    protected ScmProviderRepository getScmProviderRepository()
        throws Exception
    {
        return new GitScmProviderRepository( getScmUrl().substring( "scm:jgit:".length() ) );
    }
}
