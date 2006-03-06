package org.apache.maven.scm.provider.bazaar;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common code for executing bazaar commands.
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarUtils
{

    /**
     * Map between command and its valid exit codes
     */
    private static final Map exitCodeMap = new HashMap();

    /**
     * Default exit codes for entries not in exitCodeMap
     */
    private static final List defaultExitCodes = new ArrayList();

    /** Setup exit codes*/
    static
    {
        defaultExitCodes.add( new Integer( 0 ) );

        //Diff is different
        List diffExitCodes = new ArrayList();
        diffExitCodes.add( new Integer( 0 ) ); //No difference
        diffExitCodes.add( new Integer( 1 ) ); //Difference exists
        exitCodeMap.put( BazaarCommand.DIFF_CMD, diffExitCodes );
    }

    public static ScmResult execute( BazaarConsumer consumer, ScmLogger logger, File workingDir, String[] cmdAndArgs )
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
                String msg = "Working directory did not exist and it couldn't be created: " + workingDir;
                throw new ScmException( msg );
            }
        }

        logger.info( "Executing: " + cmd );
        logger.info( "Working directory: " + workingDir.getAbsolutePath() );

        final int exitCode;
        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cmd, consumer, consumer );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Command could not be executed: " + cmd, ex );
        }

        List exitCodes =
            exitCodeMap.containsKey( cmdAndArgs[0] ) ? (List) exitCodeMap.get( cmdAndArgs[0] ) : defaultExitCodes;
        boolean success = exitCodes.contains( new Integer( exitCode ) );

        String providerMsg = "Execution of bazaar command: " + ( success ? "succeded" : "failed" );
        return new ScmResult( cmd.toString(), providerMsg, consumer.getStdErr(), success );
    }

    public static ScmResult execute( File workingDir, String[] cmdAndArgs )
        throws ScmException
    {
        ScmLogger logger = new DefaultLog();
        return execute( new BazaarConsumer( logger ), logger, workingDir, cmdAndArgs );
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

        String[] revCmd = new String[]{BazaarCommand.REVNO_CMD};
        BazaarRevNoConsumer consumer = new BazaarRevNoConsumer( logger );
        BazaarUtils.execute( consumer, logger, workingDir, revCmd );

        return consumer.getCurrentRevisionNumber();
    }

    /**
     * Get current (working) revision.
     * <p/>
     * Resolve revision to the last integer found in the command output.
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
