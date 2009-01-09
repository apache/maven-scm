package org.apache.maven.scm.provider.svn.svnexe.command.info;

import java.io.File;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

public class SvnInfoCommandTest
    extends ScmTestCase
{

    public void testGetInfoOnEmptyFileSet()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( new File( getBasedir() ) );
        
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", fileSet, "svn --non-interactive info" );
    }
    
    private void testCommandLine( String scmUrl, ScmFileSet fileSet, String commandLine )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl = SvnInfoCommand.createCommandLine( svnRepository, fileSet, false, null );
        
        assertCommandLine( commandLine, fileSet.getBasedir(), cl );
    }
}
