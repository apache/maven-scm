package org.apache.maven.scm.provider.git.gitexe.command.info;

import java.io.File;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider;
import org.codehaus.plexus.PlexusTestCase;

public class GitInfoCommandTckTest
    extends ScmTestCase
{
    
    public void testInfoCommand() throws Exception
    {
        GitScmTestUtils.initRepo( "src/test/resources/git/info", getRepositoryRoot(), getWorkingCopy() );
        GitExeScmProvider provider = (GitExeScmProvider) getScmManager().getProviderByUrl( getScmUrl() );
        ScmProviderRepository repository = provider.makeProviderScmRepository( getRepositoryRoot() );
        assertNotNull( repository );
        InfoScmResult result = provider.info( repository, new ScmFileSet( getRepositoryRoot() ), new CommandParameters() );
        assertNotNull( result );
        assertEquals( "cd3c0dfacb65955e6fbb35c56cc5b1bf8ce4f767", result.getInfoItems().get( 0 ).getRevision() );
        // 
    }
    
    protected File getRepositoryRoot()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/repository/git/info" );
    }

    public String getScmUrl()
        throws Exception
    {
        return GitScmTestUtils.getScmUrl( getRepositoryRoot(), "git" );
    }
    
    protected File getWorkingCopy()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/git/info" );
    }    

}
