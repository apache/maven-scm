package org.apache.maven.scm.provider.git.gitexe.command.blame;

import junit.framework.Assert;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class GitBlameConsumerTest
    extends PlexusTestCase
{
    public void testConsumer()
        throws Exception
    {
        GitBlameConsumer consumer = new GitBlameConsumer( new DefaultLog() );

        File f = getTestFile( "/src/test/resources/git/blame/git-blame.out" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        Assert.assertEquals( 73, consumer.getLines().size() );

        BlameLine blameLine1 = (BlameLine) consumer.getLines().get( 11 );
        Assert.assertEquals( "96cfe5d4", blameLine1.getRevision() );
        Assert.assertEquals( "Tiago Bello Torres", blameLine1.getAuthor() );

        BlameLine blameLine2 = (BlameLine) consumer.getLines().get( 35 );
        Assert.assertEquals( "8748a722", blameLine2.getRevision() );
        Assert.assertEquals( "Tiago Bello Torres", blameLine2.getAuthor() );

    }
}
