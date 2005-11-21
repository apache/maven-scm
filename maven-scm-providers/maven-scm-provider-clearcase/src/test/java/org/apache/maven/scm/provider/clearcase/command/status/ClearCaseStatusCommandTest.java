package org.apache.maven.scm.provider.clearcase.command.status;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * 
 */
public class ClearCaseStatusCommandTest extends ScmTestCase
{
    public void testCommand()
    {
        ScmFileSet scmFileSet = new ScmFileSet( getWorkingDirectory(), new File( "test.java" ) );
        Commandline commandLine = ClearCaseStatusCommand.createCommandLine( scmFileSet );
        assertEquals( "cleartool lscheckout -r -fmt %n\\n", commandLine.toString() );
    }
}
