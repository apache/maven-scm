package org.apache.maven.scm.provider.bazaar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

/**
 * Common code for executing bazaar commands.
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarUtils
{

    /** Map between command  and its valid exitcodes */
    private static final Map exitCodeMap = new HashMap();

    /** Defualt exit codes for entries not in exitCodeMap */
    private static final List defaultExitCodes = new ArrayList();

    /** Setup exit codes*/
    static
    {
        defaultExitCodes.add( new Integer( 0 ) );

        //Diff is different
        List diffExitCodes = new ArrayList();
        diffExitCodes.add( new Integer( 0 ) ); //No difference
        diffExitCodes.add( new Integer( 1 ) ); //Difference exisits
        exitCodeMap.put( BazaarCommand.DIFF_CMD, diffExitCodes );
    }

    public static ScmResult execute( StreamConsumer consumer, ScmLogger logger, File workingDir, String[] cmdAndArgs )
        throws ScmException
    {
        Commandline cmd = new Commandline();
        cmd.setExecutable( BazaarCommand.EXEC );
        cmd.setWorkingDirectory( workingDir.getAbsolutePath() );
        cmd.addArguments( cmdAndArgs );

        if ( !workingDir.exists() )
        {
            boolean success = workingDir.mkdirs();
            if ( !success )
            {
                throw new ScmException( "Working directory did not exist and it couldn't be created: " + workingDir );
            }
        }

        logger.info( "Executing: " + cmd );
        logger.info( "Working directory: " + workingDir.getAbsolutePath() );

        final int exitCode;
        StringStreamConsumer stderr = new StringStreamConsumer();
        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cmd, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Command could not be executed: " + cmd, ex );
        }

        List exitCodes = exitCodeMap.containsKey( cmdAndArgs[0] ) ? (List) exitCodeMap.get( cmdAndArgs[0] )
                                                                 : defaultExitCodes;
        boolean success = exitCodes.contains( new Integer( exitCode ) );

        return new ScmResult( cmd.toString(), "Execution of bazaar command failed", stderr.getOutput(), success );
    }

    public static ScmResult execute( File workingDir, String[] cmdAndArgs )
        throws ScmException
    {
        return execute( new StringStreamConsumer(), new DefaultLog(), workingDir, cmdAndArgs );
    }

    public static String[] expandCommandLine( String[] cmdAndArgs, ScmFileSet additionalFiles )
    {
        File[] files = additionalFiles.getFiles();
        String[] cmd = new String[files.length + cmdAndArgs.length];

        // Copy command into array
        System.arraycopy( cmdAndArgs, 0, cmd, 0, cmdAndArgs.length );

        // Add files as additional parameter into the array
        for ( int i = 0; i < files.length; i++ )
        {
            String file = files[i].getPath().replace( '\\', File.separatorChar );
            cmd[i + cmdAndArgs.length] = file;
        }

        return cmd;
    }

    public static int getCurrentRevisionNumber( ScmLogger logger, File workingDir )
        throws ScmException
    {

        String[] revCmd = new String[] { BazaarCommand.REVNO_CMD };
        BazaarRevNoConsumer consumer = new BazaarRevNoConsumer( logger );
        BazaarUtils.execute( consumer, logger, workingDir, revCmd );

        return consumer.getCurrentRevisionNumber();
    }

    /**
     * Get current (working) revision.
     *
     * Resolves revision to the last integer found in the command output.
     */
    private static class BazaarRevNoConsumer
        extends BazaarConsumer
    {

        private int revNo;

        BazaarRevNoConsumer( ScmLogger logger )
        {
            super( logger );
        }

        public void doConsume( ScmFileStatus status, String line )
        {
            try
            {
                revNo = Integer.valueOf( line ).intValue();
            }
            catch ( NumberFormatException e )
            {
                // ignore
            }
        }

        int getCurrentRevisionNumber()
        {
            return revNo;
        }
    }
}
