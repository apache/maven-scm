package org.apache.maven.scm.provider.svn.svnexe.command.export;

import java.io.File;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

public class SvnExportCommandTest
    extends ScmTestCase
{

    public void testGetExportCommandLine()
        throws Exception
    {
        File exportDirectory = new File( getBasedir() );
        
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", exportDirectory, "svn --non-interactive export --force scm:svn:http://foo.com/svn/trunk " + exportDirectory );
    }
    
    private void testCommandLine( String scmUrl, File exportDirectory, String commandLine )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl = SvnExeExportCommand.createCommandLine( svnRepository, null, scmUrl, exportDirectory.getAbsolutePath() );
        
        assertCommandLine( commandLine, exportDirectory, cl );
    }
}
