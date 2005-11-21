package org.apache.maven.scm.provider.clearcase.command.remove;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * 
 */
public class ClearCaseRemoveCommandTest extends ScmTestCase
{
    public void testCommand()
    {
        ScmFileSet scmFileSet = new ScmFileSet( getWorkingDirectory(), new File( "test.java" ) );
        Commandline commandLine = ClearCaseRemoveCommand.createCommandLine( new DefaultLog(), scmFileSet );
        assertEquals( "cleartool rmname -nc test.java", commandLine.toString() );
    }
}
