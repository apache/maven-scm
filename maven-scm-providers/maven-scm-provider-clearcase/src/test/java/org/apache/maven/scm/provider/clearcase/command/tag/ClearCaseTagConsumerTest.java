package org.apache.maven.scm.provider.clearcase.command.tag;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;

import java.io.*;
import java.util.Collection;

/**
 * 
 */
public class ClearCaseTagConsumerTest extends ScmTestCase
{
    public void testConsumer() throws IOException
    {
        InputStream inputStream = getResourceAsStream( "/clearcase/tag/tag.txt" );

        BufferedReader in = new BufferedReader( new InputStreamReader( inputStream ) );

        String s = in.readLine();

        ClearCaseTagConsumer consumer = new ClearCaseTagConsumer( new DefaultLog() );

        while (s != null)
        {
            consumer.consumeLine( s );

            s = in.readLine();
        }

        Collection entries = consumer.getTaggedFiles();

        assertEquals( "Wrong number of entries returned", 1, entries.size() );

        ScmFile scmFile = (ScmFile)entries.iterator().next();
        assertEquals( "test.java", scmFile.getPath() );
        assertEquals( ScmFileStatus.TAGGED, scmFile.getStatus() );
    }
}
