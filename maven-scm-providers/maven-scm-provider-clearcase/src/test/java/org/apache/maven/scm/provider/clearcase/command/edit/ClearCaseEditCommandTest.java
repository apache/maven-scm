package org.apache.maven.scm.provider.clearcase.command.edit;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * 
 */
public class ClearCaseEditCommandTest extends ScmTestCase
{
    public void testCommand()
    {
        ScmFileSet scmFileSet = new ScmFileSet( getWorkingDirectory(), new File( "test.java" ) );
        Commandline commandLine = ClearCaseEditCommand.createCommandLine( new DefaultLog(), scmFileSet );
        assertEquals( "cleartool co -nc test.java", commandLine.toString() );
    }
}
