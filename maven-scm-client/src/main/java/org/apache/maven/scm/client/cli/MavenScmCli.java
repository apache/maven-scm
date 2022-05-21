package org.apache.maven.scm.client.cli;

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

import java.io.File;
import java.util.List;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.embed.Embedder;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class MavenScmCli
{
    private Embedder plexus;

    private ScmManager scmManager;

    // ----------------------------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------------------------

    public MavenScmCli()
        throws Exception
    {
        plexus = new Embedder();

        plexus.start();

        scmManager = (ScmManager) plexus.lookup( ScmManager.ROLE );
    }

    public void stop()
    {
        try
        {
            plexus.stop();
        }
        catch ( Exception ex )
        {
            // ignore
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static void main( String[] args )
    {
        MavenScmCli cli;

        try
        {
            cli = new MavenScmCli();
        }
        catch ( Exception ex )
        {
            System.err.println( "Error while starting Maven SCM." );

            ex.printStackTrace( System.err );

            return;
        }

        String scmUrl;

        String command;

        if ( args.length < 3 )
        {
            System.err.println(
                "Usage: maven-scm-client <command> <working directory> <scm url> [<scmVersion> [<scmVersionType>]]" );
            System.err.println( "scmVersion is a branch name/tag name/revision number." );
            System.err.println( "scmVersionType can be 'branch', 'tag', 'revision'. "
                + "The default value is 'revision'." );

            return;
        }

        command = args[0];

        // SCM-641
        File workingDirectory = new File( args[1] ).getAbsoluteFile();

        scmUrl = args[2];

        ScmVersion scmVersion = null;
        if ( args.length > 3 )
        {
            String version = args[3];

            if ( args.length > 4 )
            {
                String type = args[4];

                if ( "tag".equals( type ) )
                {
                    scmVersion = new ScmTag( version );
                }
                else if ( "branch".equals( type ) )
                {
                    scmVersion = new ScmBranch( version );
                }
                else if ( "revision".equals( type ) )
                {
                    scmVersion = new ScmRevision( version );
                }
                else
                {
                    throw new IllegalArgumentException( "'" + type + "' version type isn't known." );
                }
            }
            else
            {
                scmVersion = new ScmRevision( args[3] );
            }
        }

        cli.execute( scmUrl, command, workingDirectory, scmVersion );

        cli.stop();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void execute( String scmUrl, String command, File workingDirectory, ScmVersion version )
    {
        ScmRepository repository;

        try
        {
            repository = scmManager.makeScmRepository( scmUrl );
        }
        catch ( NoSuchScmProviderException ex )
        {
            System.err.println( "Could not find a provider." );

            return;
        }
        catch ( ScmRepositoryException ex )
        {
            System.err.println( "Error while connecting to the repository" );

            ex.printStackTrace( System.err );

            return;
        }

        try
        {
            if ( command.equals( "checkout" ) )
            {
                checkOut( repository, workingDirectory, version );
            }
            else if ( command.equals( "checkin" ) )
            {
                checkIn( repository, workingDirectory, version );
            }
            else if ( command.equals( "update" ) )
            {
                update( repository, workingDirectory, version );
            }
            else
            {
                System.err.println( "Unknown SCM command '" + command + "'." );
            }
        }
        catch ( ScmException ex )
        {
            System.err.println( "Error while executing the SCM command." );

            ex.printStackTrace( System.err );

            return;
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void checkOut( ScmRepository scmRepository, File workingDirectory, ScmVersion version )
        throws ScmException
    {
        if ( workingDirectory.exists() )
        {
            System.err.println( "The working directory already exist: '" + workingDirectory.getAbsolutePath()
                + "'." );

            return;
        }

        if ( !workingDirectory.mkdirs() )
        {
            System.err.println(
                "Error while making the working directory: '" + workingDirectory.getAbsolutePath() + "'." );

            return;
        }

        CheckOutScmResult result = scmManager.checkOut( scmRepository, new ScmFileSet( workingDirectory ), version );

        if ( !result.isSuccess() )
        {
            showError( result );

            return;
        }

        List<ScmFile> checkedOutFiles = result.getCheckedOutFiles();

        System.out.println( "Checked out these files: " );

        for ( ScmFile file : checkedOutFiles )
        {
            System.out.println( " " + file.getPath() );
        }
    }

    private void checkIn( ScmRepository scmRepository, File workingDirectory, ScmVersion version )
        throws ScmException
    {
        if ( !workingDirectory.exists() )
        {
            System.err.println( "The working directory doesn't exist: '" + workingDirectory.getAbsolutePath()
                + "'." );

            return;
        }

        String message = "";

        CheckInScmResult result =
            scmManager.checkIn( scmRepository, new ScmFileSet( workingDirectory ), version, message );

        if ( !result.isSuccess() )
        {
            showError( result );

            return;
        }

        List<ScmFile> checkedInFiles = result.getCheckedInFiles();

        System.out.println( "Checked in these files: " );

        for ( ScmFile file : checkedInFiles )
        {
            System.out.println( " " + file.getPath() );
        }
    }

    private void update( ScmRepository scmRepository, File workingDirectory, ScmVersion version )
        throws ScmException
    {
        if ( !workingDirectory.exists() )
        {
            System.err.println( "The working directory doesn't exist: '" + workingDirectory.getAbsolutePath()
                + "'." );

            return;
        }

        UpdateScmResult result = scmManager.update( scmRepository, new ScmFileSet( workingDirectory ), version );

        if ( !result.isSuccess() )
        {
            showError( result );

            return;
        }

        List<ScmFile> updatedFiles = result.getUpdatedFiles();

        System.out.println( "Updated these files: " );

        for ( ScmFile file : updatedFiles )
        {
            System.out.println( " " + file.getPath() );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void showError( ScmResult result )
    {
        System.err.println( "There was a error while executing the SCM command." );

        String providerMessage = result.getProviderMessage();

        if ( !StringUtils.isEmpty( providerMessage ) )
        {
            System.err.println( "Error message from the provider: " + providerMessage );
        }
        else
        {
            System.err.println( "The provider didn't give a error message." );
        }

        String output = result.getCommandOutput();

        if ( !StringUtils.isEmpty( output ) )
        {
            System.err.println( "Command output:" );

            System.err.println( output );
        }
    }
}
