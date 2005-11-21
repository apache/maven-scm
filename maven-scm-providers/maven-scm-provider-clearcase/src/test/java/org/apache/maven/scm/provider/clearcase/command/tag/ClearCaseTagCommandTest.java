package org.apache.maven.scm.provider.clearcase.command.tag;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * 
 */
public class ClearCaseTagCommandTest extends ScmTestCase
{
    public void testCommand()
    {
        ScmFileSet scmFileSet = new ScmFileSet( getWorkingDirectory(), new File( "test.java" ) );
        Commandline commandLine = ClearCaseTagCommand.createCommandLine( scmFileSet, "TEST_LABEL_V1.0" );
        assertEquals( "cleartool mklabel TEST_LABEL_V1.0 test.java", commandLine.toString() );
    }
}
