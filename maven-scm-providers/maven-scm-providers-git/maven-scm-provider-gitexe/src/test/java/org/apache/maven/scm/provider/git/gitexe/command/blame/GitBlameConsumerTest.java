package org.apache.maven.scm.provider.git.gitexe.command.blame;

import junit.framework.Assert;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Test the {@link GitBlameConsumer} in various different situations.
 * Depending on the underlying operating system we might get
 * slightly different output from a <pre>git blame</pre> commandline invocation.
 */
public class GitBlameConsumerTest
    extends PlexusTestCase
{
    public void testConsumerEasy()
        throws Exception
    {
        GitBlameConsumer consumer = consumeFile( "/src/test/resources/git/blame/git-blame-3.out" );

        Assert.assertEquals( 36, consumer.getLines().size() );

        BlameLine blameLine = (BlameLine) consumer.getLines().get( 11 );
        Assert.assertEquals( "e670863b2b03e158c59f34af1fee20f91b2bd852", blameLine.getRevision() );
        Assert.assertEquals( "Mark Struberg", blameLine.getAuthor() );
        Assert.assertNotNull( blameLine.getDate() );
    }


    public void testConsumer()
        throws Exception
    {
        GitBlameConsumer consumer = consumeFile( "/src/test/resources/git/blame/git-blame.out" );

        Assert.assertEquals( 187, consumer.getLines().size() );

        BlameLine blameLine = (BlameLine) consumer.getLines().get( 11 );
        Assert.assertEquals( "e670863b2b03e158c59f34af1fee20f91b2bd852", blameLine.getRevision() );
        Assert.assertEquals( "Mark Struberg", blameLine.getAuthor() );
        Assert.assertNotNull( blameLine.getDate() );
    }

    /**
     * Test what happens if a git-blame command got invoked on a
     * file which has no content.
     */
    public void testConsumerEmptyFile()
        throws Exception
    {
        GitBlameConsumer consumer = consumeFile( "/src/test/resources/git/blame/git-blame-empty.out" );

        Assert.assertEquals( 0, consumer.getLines().size() );
    }


    /**
     * Test what happens if a git-blame command got invoked on a
     * file which didn't got added to the git repo yet.
     */
    public void testConsumerOnNewFile()
        throws Exception
    {
        GitBlameConsumer consumer = consumeFile( "/src/test/resources/git/blame/git-blame-new-file.out" );

        Assert.assertEquals( 3, consumer.getLines().size() );
        BlameLine blameLine = (BlameLine) consumer.getLines().get( 0 );
        Assert.assertNotNull(blameLine);
        Assert.assertEquals( "0000000000000000000000000000000000000000", blameLine.getRevision() );
        Assert.assertEquals("Not Committed Yet", blameLine.getAuthor());

    }

    /**
     * Consume all lines in the given file with a fresh {@link GitBlameConsumer}.
     *
     * @param fileName
     * @return the resulting {@link GitBlameConsumer}
     * @throws IOException
     */
    private GitBlameConsumer consumeFile( String fileName ) throws IOException
    {
        GitBlameConsumer consumer = new GitBlameConsumer( new DefaultLog() );

        File f = getTestFile( fileName );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }
        return consumer;
    }
}
