package org.apache.maven.scm.provider.svn.svnexe.command.export;

import java.io.File;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

public class SvnExportCommandTest
    extends ScmTestCase
{

    public void testGetExportCommandLineWithImplicitExportDirectory()
        throws Exception
    {
        File exportDirectory = new File( getBasedir() );

        testCommandLine( "scm:svn:http://foo.com/svn/trunk", exportDirectory, null,
                         "svn --non-interactive export --force scm:svn:http://foo.com/svn/trunk" );
    }

    public void testGetExportCommandLineWithExplicitExportDirectory()
        throws Exception
    {
        File exportDirectory = new File( getBasedir() );

        testCommandLine( "scm:svn:http://foo.com/svn/trunk", exportDirectory, exportDirectory,
                         "svn --non-interactive export --force scm:svn:http://foo.com/svn/trunk " + exportDirectory );
    }

    private void testCommandLine( String scmUrl, File workingDirectory, File exportDirectory, String commandLine )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl = SvnExeExportCommand.createCommandLine( svnRepository, exportDirectory, null, scmUrl,
                                                                exportDirectory != null?exportDirectory.getAbsolutePath():null );

        assertCommandLine( commandLine, exportDirectory, cl );
    }
}
