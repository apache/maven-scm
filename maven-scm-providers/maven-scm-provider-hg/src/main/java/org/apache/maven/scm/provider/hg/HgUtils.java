package org.apache.maven.scm.provider.hg;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.command.inventory.HgChangeSet;
import org.apache.maven.scm.provider.hg.command.inventory.HgOutgoingConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Common code for executing hg commands.
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @version $Id$
 */
public class HgUtils
{
    /**
     * Map between command and its valid exit codes
     */
    private static final Map<String,List<Integer>> EXIT_CODE_MAP = new HashMap<String,List<Integer>>();

    /**
     * Default exit codes for entries not in exitCodeMap
     */
    private static final List<Integer> DEFAULT_EXIT_CODES = new ArrayList<Integer>();

    /** Setup exit codes*/
    static
    {
        DEFAULT_EXIT_CODES.add( new Integer( 0 ) );

        //Diff is different
        List<Integer> diffExitCodes = new ArrayList<Integer>( 3 );
        diffExitCodes.add( Integer.valueOf( 0 ) ); //No difference
        diffExitCodes.add( Integer.valueOf( 1 ) ); //Conflicts in merge-like or changes in diff-like
        diffExitCodes.add( Integer.valueOf( 2 ) ); //Unrepresentable diff changes
        EXIT_CODE_MAP.put( HgCommandConstants.DIFF_CMD, diffExitCodes );
        //Outgoing is different
        List<Integer> outgoingExitCodes = new ArrayList<Integer>( 2 );
        outgoingExitCodes.add( new Integer( 0 ) ); //There are changes
        outgoingExitCodes.add( new Integer( 1 ) ); //No changes
        EXIT_CODE_MAP.put( HgCommandConstants.OUTGOING_CMD, outgoingExitCodes );        
    }

    public static ScmResult execute( HgConsumer consumer, ScmLogger logger, File workingDir, String[] cmdAndArgs )
        throws ScmException
    {
        try
        {
            //Build commandline
            Commandline cmd = buildCmd( workingDir, cmdAndArgs );
            if ( logger.isInfoEnabled() )
            {
                logger.info( "EXECUTING: " + cmd );
            }

            //Execute command
            int exitCode = executeCmd( consumer, cmd );

            //Return result
            List<Integer> exitCodes = DEFAULT_EXIT_CODES;
            if ( EXIT_CODE_MAP.containsKey( cmdAndArgs[0] ) )
            {
                exitCodes = EXIT_CODE_MAP.get( cmdAndArgs[0] );
            }
            boolean success = exitCodes.contains( new Integer( exitCode ) );

            //On failure (and not due to exceptions) - run diagnostics
            String providerMsg = "Execution of hg command succeded";
            if ( !success )
            {
                HgConfig config = new HgConfig( workingDir );
                providerMsg =
                    "\nEXECUTION FAILED" + "\n  Execution of cmd : " + cmdAndArgs[0] + " failed with exit code: " +
                        exitCode + "." + "\n  Working directory was: " + "\n    " + workingDir.getAbsolutePath() +
                        config.toString( workingDir ) + "\n";
                if ( logger.isErrorEnabled() )
                {
                    logger.error( providerMsg );
                }
            }

            return new ScmResult( cmd.toString(), providerMsg, consumer.getStdErr(), success );
        }
        catch ( ScmException se )
        {
            String msg =
                "EXECUTION FAILED" + "\n  Execution failed before invoking the Hg command. Last exception:" + "\n    " +
                    se.getMessage();

            //Add nested cause if any
            if ( se.getCause() != null )
            {
                msg += "\n  Nested exception:" + "\n    " + se.getCause().getMessage();
            }

            //log and return
            if ( logger.isErrorEnabled() )
            {
                logger.error( msg );
            }
            throw se;
        }
    }

    static Commandline buildCmd( File workingDir, String[] cmdAndArgs )
        throws ScmException
    {
        Commandline cmd = new Commandline();
        cmd.setExecutable( HgCommandConstants.EXEC );
        cmd.setWorkingDirectory( workingDir.getAbsolutePath() );
        cmd.addArguments( cmdAndArgs );

        if ( !workingDir.exists() )
        {
            boolean success = workingDir.mkdirs();
            if ( !success )
            {
                String msg = "Working directory did not exist" + " and it couldn't be created: " + workingDir;
                throw new ScmException( msg );
            }
        }
        return cmd;
    }

    static int executeCmd( HgConsumer consumer, Commandline cmd )
        throws ScmException
    {
        final int exitCode;
        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cmd, consumer, consumer );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Command could not be executed: " + cmd, ex );
        }
        return exitCode;
    }

    public static ScmResult execute( File workingDir, String[] cmdAndArgs )
        throws ScmException
    {
        ScmLogger logger = new DefaultLog();
        return execute( new HgConsumer( logger ), logger, workingDir, cmdAndArgs );
    }

    public static String[] expandCommandLine( String[] cmdAndArgs, ScmFileSet additionalFiles )
    {
        List<File> filesList = additionalFiles.getFileList();
        String[] cmd = new String[filesList.size() + cmdAndArgs.length];

        // Copy command into array
        System.arraycopy( cmdAndArgs, 0, cmd, 0, cmdAndArgs.length );

        // Add files as additional parameter into the array
        int i = 0;
        for ( Iterator<File> iterator = filesList.iterator(); iterator.hasNext(); i++ )
        {
            File scmFile = (File) iterator.next();
            String file = scmFile.getPath().replace( '\\', File.separatorChar );
            cmd[i + cmdAndArgs.length] = file;

        }

        return cmd;
    }

    public static int getCurrentRevisionNumber( ScmLogger logger, File workingDir )
        throws ScmException
    {

        String[] revCmd = new String[]{ HgCommandConstants.REVNO_CMD };
        HgRevNoConsumer consumer = new HgRevNoConsumer( logger );
        HgUtils.execute( consumer, logger, workingDir, revCmd );

        return consumer.getCurrentRevisionNumber();
    }

    public static String getCurrentBranchName( ScmLogger logger, File workingDir )
        throws ScmException
    {
        String[] branchnameCmd = new String[]{ HgCommandConstants.BRANCH_NAME_CMD };
        HgBranchnameConsumer consumer = new HgBranchnameConsumer( logger );
        HgUtils.execute( consumer, logger, workingDir, branchnameCmd );
        return consumer.getBranchName();
    }

    /**
     * Get current (working) revision.
     * <p/>
     * Resolve revision to the last integer found in the command output.
     */
    private static class HgRevNoConsumer
        extends HgConsumer
    {

        private int revNo;

        HgRevNoConsumer( ScmLogger logger )
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

    /**
     * Get current (working) branch name
     */
    private static class HgBranchnameConsumer
        extends HgConsumer
    {

        private String branchName;

        HgBranchnameConsumer( ScmLogger logger )
        {
            super( logger );
        }

        public void doConsume( ScmFileStatus status, String trimmedLine )
        {
            branchName = String.valueOf( trimmedLine );
        }

        String getBranchName()
        {
            return branchName;
        }
    }


    /**
     * Check if there are outgoing changes on a different branch. If so, Mercurial default behaviour
     * is to block the push and warn using a 'push creates new remote branch !' message.
     * We also warn, and return true if a different outgoing branch was found
     * <p/>
     * Method users should not stop the push on a negative return, instead, they should hg push -r(branch being released)
     *
     * @param logger            the logger
     * @param workingDir        the working dir
     * @param workingbranchName the working branch name
     * @return true if a different outgoing branch was found
     * @throws ScmException on outgoing command error
     */
    public static boolean differentOutgoingBranchFound( ScmLogger logger, File workingDir, String workingbranchName )
        throws ScmException
    {
        String[] outCmd = new String[]{ HgCommandConstants.OUTGOING_CMD };
        HgOutgoingConsumer outConsumer = new HgOutgoingConsumer( logger );
        ScmResult outResult = HgUtils.execute( outConsumer, logger, workingDir, outCmd );
        List<HgChangeSet> changes = outConsumer.getChanges();
        if ( outResult.isSuccess() )
        {
            for ( int i = 0; i < changes.size(); i++ )
            {
                HgChangeSet set = changes.get( i );
                if ( set.getBranch() != null )
                {
                    logger.warn( "A different branch than " + workingbranchName +
                        " was found in outgoing changes, branch name was " + set.getBranch() +
                        ". Only local branch named " + workingbranchName + " will be pushed." );
                    return true;
                }
            }
        }
        return false;
    }
}
