package org.apache.maven.scm.provider.clearcase.command.unedit;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * 
 */
public class ClearCaseUnEditCommandTest extends ScmTestCase
{
    public void testCommand()
    {
        ScmFileSet scmFileSet = new ScmFileSet( getWorkingDirectory(), new File( "test.java" ) );
        Commandline commandLine = ClearCaseUnEditCommand.createCommandLine( new DefaultLog(), scmFileSet );
        assertEquals( "cleartool unco -keep test.java", commandLine.toString() );
    }
}
