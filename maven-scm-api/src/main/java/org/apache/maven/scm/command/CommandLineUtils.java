package org.apache.maven.scm.command;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public abstract class CommandLineUtils
{/*
    public static class StringStreamConsumer
        implements StreamConsumer
    {
        private StringBuffer string = new StringBuffer();

        private String ls = System.getProperty( "line.separator" );

        public void consumeLine( String line )
        {
            string.append( line + ls );
        }

        public String getOutput()
        {
            return string.toString();
        }
    }

    public static int executeCommandLine( Commandline cl, StreamConsumer systemOut, StreamConsumer systemErr )
        throws ScmException
    {
        if ( cl == null )
        {
            throw new ScmException( "The command line cannot be null." );
        }

        Process p;

        System.out.println( "Executing: " + cl );
        System.out.println( "pwd: " + cl.getWorkingDirectory().getAbsolutePath() );

        try
        {
            p = cl.execute();
        }
        catch( IOException ex )
        {
            throw new ScmException( "Error while executing external command.", ex );
        }

        StreamPumper inputPumper = new StreamPumper( p.getInputStream(), systemOut );

        StreamPumper errorPumper = new StreamPumper( p.getErrorStream(), systemErr );

        inputPumper.start();

        errorPumper.start();

        try
        {
            int returnValue = p.waitFor();

            while( !inputPumper.isDone() )
            {
                Thread.sleep( 0 );
            }

            while( !errorPumper.isDone() )
            {
                Thread.sleep( 0 );
            }

            inputPumper.close();

            errorPumper.close();

            return returnValue;
        }
        catch( InterruptedException ex )
        {
            throw new ScmException( "Error while executing external command.", ex );
        }
    }
*/
}
