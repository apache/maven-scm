package org.apache.maven.scm.provider.clearcase.command.checkin;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * 
 */
public class ClearCaseCheckInCommandTest extends ScmTestCase
{
    public void testCommand()
    {
        ScmFileSet scmFileSet = new ScmFileSet( getWorkingDirectory(), new File( "test.java" ) );
        Commandline commandLine = ClearCaseCheckInCommand.createCommandLine( scmFileSet, "done some changes" );

        assertEquals( "cleartool ci -c \"done some changes\" test.java", commandLine.toString() );
    }
}
