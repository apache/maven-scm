package org.apache.maven.scm.provider.clearcase.command.add;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * 
 */
public class ClearCaseAddCommandTest extends ScmTestCase
{
    public void testAddCommand()
    {
        ScmFileSet scmFileSet = new ScmFileSet( getWorkingDirectory(), new File( "test.java" ) );
        Commandline commandLine = ClearCaseAddCommand.createCommandLine( scmFileSet );
        assertEquals( "cleartool mkelem -c \"new file\" -nco test.java", commandLine.toString() );
    }
}
