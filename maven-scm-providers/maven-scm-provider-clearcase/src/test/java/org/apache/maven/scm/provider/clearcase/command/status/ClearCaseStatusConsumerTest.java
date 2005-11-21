package org.apache.maven.scm.provider.clearcase.command.status;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;

import java.io.*;
import java.util.Collection;

/**
 * 
 */
public class ClearCaseStatusConsumerTest extends ScmTestCase
{
    public void testConsumer() throws IOException
    {
        InputStream inputStream = getResourceAsStream( "/clearcase/status/status.txt" );

        BufferedReader in = new BufferedReader( new InputStreamReader( inputStream ) );

        String s = in.readLine();

        ClearCaseStatusConsumer consumer = new ClearCaseStatusConsumer( new DefaultLog(), getWorkingDirectory() );

        while (s != null)
        {
            consumer.consumeLine( s );

            s = in.readLine();
        }

        Collection entries = consumer.getCheckedOutFiles();

        assertEquals( "Wrong number of entries returned", 1, entries.size() );

        ScmFile scmFile = (ScmFile)entries.iterator().next();
        assertEquals( new File( getWorkingDirectory(), "test.java").getAbsolutePath(), scmFile.getPath() );
        assertEquals( ScmFileStatus.CHECKED_OUT, scmFile.getStatus() );
    }
}
