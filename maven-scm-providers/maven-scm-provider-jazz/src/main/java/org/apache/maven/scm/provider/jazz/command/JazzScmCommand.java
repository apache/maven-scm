package org.apache.maven.scm.provider.jazz.command;

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
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.Iterator;

/**
 * The base class for the underlying jazz "scm.sh"/"scm.exe" command.
 * <p/>
 * The SCM command is documented here:
 * <p/>
 * V2.0.2: http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_scm.html
 * V3.0:   http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_scm.html
 * V3.0.1: http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_scm.html
 *
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzScmCommand
{
    // The logger to use.
    private ScmLogger fLogger;

    // The Commandline that we build up and execute.
    private Commandline fCommand;

    /**
     * Create a JazzScmCommand when no sub-command is needed.
     *
     * @throws ScmException
     */
    public JazzScmCommand( String cmd, ScmProviderRepository repo, ScmFileSet fileSet, ScmLogger logger )
    {
        this( cmd, null, repo, true, fileSet, logger );
    }

    /**
     * Create a JazzScmCommand when a sub-command is needed.
     * eg: "create snapshot ..."
     */
    public JazzScmCommand( String cmd, String subCmd, ScmProviderRepository repo, ScmFileSet fileSet, ScmLogger logger )
    {
        this( cmd, subCmd, repo, true, fileSet, logger );
    }

    /**
     * Create a JazzScmCommand, adding the repository-uri as needed.
     */
    public JazzScmCommand( String cmd, String subCmd, ScmProviderRepository repo, boolean addRepositoryWorkspaceArg,
                           ScmFileSet fileSet, ScmLogger logger )
    {
        fLogger = logger;
        fCommand = new Commandline();

        // TODO This was developed and tested in Windows (in which scm (scm.exe) was the valid executable)
        // Verify that the executable is valid in other operating systems.
        fCommand.setExecutable( JazzConstants.SCM_EXECUTABLE );

        if ( fileSet != null )
        {
            fCommand.setWorkingDirectory( fileSet.getBasedir().getAbsolutePath() );

            // Make the directory, if need be.
            if ( !fCommand.getWorkingDirectory().exists() )
            {
                boolean success = fCommand.getWorkingDirectory().mkdirs();
                if ( !success )
                {
                    // Just log the error, don't throw an error, as it is going to fail anyway.
                    logErrorMessage( "Working directory did not exist" + " and it couldn't be created: "
                                         + fCommand.getWorkingDirectory() );
                }
            }
        }

        // Add the main command
        if ( !StringUtils.isEmpty( cmd ) )
        {
            addArgument( cmd );
        }

        // Add the sub-command if present
        if ( !StringUtils.isEmpty( subCmd ) )
        {
            addArgument( subCmd );
        }

        JazzScmProviderRepository jazzRepo = (JazzScmProviderRepository) repo;

        // Add the repository argument if needed (most commands need this, but not all)
        if ( addRepositoryWorkspaceArg )
        {
            String repositoryWorkspace = jazzRepo.getRepositoryURI();
            if ( !StringUtils.isEmpty( repositoryWorkspace ) )
            {
                addArgument( JazzConstants.ARG_REPOSITORY_URI );
                addArgument( jazzRepo.getRepositoryURI() );
            }
        }

        // Add the username argument
        // TODO Figure out how we would use the login command / username caching so this is not required on each
        // command.
        String user = jazzRepo.getUser();
        if ( !StringUtils.isEmpty( user ) )
        {
            addArgument( JazzConstants.ARG_USER_NAME );
            addArgument( jazzRepo.getUser() );
        }

        // Add the password argument
        // TODO Figure out how we would use the login command / password caching so this is not required on each
        // command.
        String password = jazzRepo.getPassword();
        if ( !StringUtils.isEmpty( password ) )
        {
            addArgument( JazzConstants.ARG_USER_PASSWORD );
            addArgument( jazzRepo.getPassword() );
        }
    }

    public void addArgument( ScmFileSet fileSet )
    {
        logInfoMessage( "files: " + fileSet.getBasedir().getAbsolutePath() );
        Iterator<File> iter = fileSet.getFileList().iterator();
        while ( iter.hasNext() )
        {
            fCommand.createArg().setValue(  iter.next().getPath() );
        }
    }

    public void addArgument( String arg )
    {
        fCommand.createArg().setValue( arg );
    }

    public int execute( StreamConsumer out, ErrorConsumer err )
        throws ScmException
    {
        logInfoMessage( "Executing: " + cryptPassword( fCommand ) );
        if ( fCommand.getWorkingDirectory() != null )
        {
            logInfoMessage( "Working directory: " + fCommand.getWorkingDirectory().getAbsolutePath() );
        }

        int status = 0;
        try
        {
            status = CommandLineUtils.executeCommandLine( fCommand, out, err );
        }
        catch ( CommandLineException e )
        {
            String errorOutput = err.getOutput();
            if ( errorOutput.length() > 0 )
            {
                logErrorMessage( "Error: " + err.getOutput() );
            }
            throw new ScmException( "Error while executing Jazz SCM command line - " + getCommandString(), e );
        }
        String errorOutput = err.getOutput();
        if ( errorOutput.length() > 0 )
        {
            logErrorMessage( "Error: " + err.getOutput() );
        }
        return status;
    }

    public String getCommandString()
    {
        return fCommand.toString();
    }

    public Commandline getCommandline()
    {
        return fCommand;
    }

    private void logErrorMessage( String message )
    {
        if ( fLogger != null )
        {
            fLogger.error( message );
        }
    }

    private void logInfoMessage( String message )
    {
        if ( fLogger != null )
        {
            fLogger.info( message );
        }
    }

    private void logDebugMessage( String message )
    {
        if ( fLogger != null )
        {
            fLogger.debug( message );
        }
    }

    // Unashamedly 'borrowed' from SvnCommandLineUtils
    // (but fixed for cases where the line ends in the password (no trailing space or further input).
    public static String cryptPassword( Commandline cl )
    {
        String clString = cl.toString();

        int pos = clString.indexOf( "--password" );

        if ( pos > 0 )
        {
            String beforePassword = clString.substring( 0, pos + "--password ".length() );
            String afterPassword = clString.substring( pos + "--password ".length() );
            pos = afterPassword.indexOf( ' ' );
            if ( pos > 0 )
            {
                afterPassword = afterPassword.substring( pos );
            }
            else
            {
                if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
                {
                    afterPassword = "\"";
                }
                else
                {
                    afterPassword = "";
                }
            }
            if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
            {
                clString = beforePassword + "*****" + afterPassword;
            }
            else
            {
                clString = beforePassword + "'*****'" + afterPassword;
            }
        }

        return clString;
    }

    /**
     * Check if the exit status is meant to be an error:
     * https://jazz.net/help-dev/clm/index.jsp?topic=%2Fcom.ibm.team.scm.doc%2Ftopics%2Fr_scm_cli_retcodes.html
     */
    public static boolean isCommandExitError(int status) {
        return status != 0 && status != 52 && status != 53;
    }
}
