package org.apache.maven.scm.client.cli;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.File;
import java.util.List;
import java.util.Iterator;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import org.codehaus.plexus.embed.Embedder;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
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
        catch( Exception ex )
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
        catch( Exception ex )
        {
            System.err.println( "Error while starting Maven Scm." );

            ex.printStackTrace( System.err  );

            return;
        }

        String scmUrl;

        String command;

        if ( args.length != 3 )
        {
            System.err.println( "Usage: maven-scm-client <command> <working directory> <scm url>" );

            return;
        }

        command = args[ 0 ];

        File workingDirectory = new File( args[ 1 ] );

        scmUrl = args[ 2 ];

        String tag = null;

        cli.execute( scmUrl, command, workingDirectory, tag );

        cli.stop();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void execute( String scmUrl, String command, File workingDirectory, String tag )
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
        catch( ScmRepositoryException ex )
        {
            System.err.println( "Error while connecting to the repository" );

            ex.printStackTrace( System.err );

            return;
        }

        try
        {
            if ( command.equals( "checkout" ) )
            {
                checkOut( repository, workingDirectory, tag );
            }
            else if ( command.equals( "checkin" ) )
            {
                checkIn( repository, workingDirectory, tag );
            }
            else if ( command.equals( "update" ) )
            {
                update( repository, workingDirectory, tag );
            }
            else
            {
                System.err.println( "Unknown SCM command '" + command + "'." );
            }
        }
        catch( ScmException ex )
        {
            System.err.println( "Error while executing the SCM command." );
            
            ex.printStackTrace( System.err );

            return;
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void checkOut( ScmRepository scmRepository, File workingDirectory, String tag )
        throws ScmException
    {
        if ( workingDirectory.exists() )
        {
            System.err.println( "The working directory already exist: '" + workingDirectory.getAbsolutePath() + "'." );

            return;
        }

        if ( !workingDirectory.mkdirs() )
        {
            System.err.println( "Error while making the working directory: '" + workingDirectory.getAbsolutePath() + "'." );

            return;
        }

        CheckOutScmResult result = scmManager.checkOut( scmRepository, new ScmFileSet( workingDirectory ), tag );

        if ( !result.isSuccess() )
        {
            showError( result );

            return;
        }

        List checkedOutFiles = result.getCheckedOutFiles();

        System.out.println( "Checked out these files: " );

        for ( Iterator it = checkedOutFiles.iterator(); it.hasNext(); )
        {
            ScmFile file = (ScmFile) it.next();

            System.out.println( " " + file.getPath() );
        }
    }

    private void checkIn( ScmRepository scmRepository, File workingDirectory, String tag )
        throws ScmException
    {
        if ( !workingDirectory.exists() )
        {
            System.err.println( "The working directory doesn't exist: '" + workingDirectory.getAbsolutePath() + "'." );

            return;
        }

        String message = "";

        CheckInScmResult result = scmManager.checkIn( scmRepository, new ScmFileSet( workingDirectory ), tag, message );

        if ( !result.isSuccess() )
        {
            showError( result );

            return;
        }

        List checkedInFiles = result.getCheckedInFiles();

        System.out.println( "Checked in these files: " );

        for ( Iterator it = checkedInFiles.iterator(); it.hasNext(); )
        {
            ScmFile file = (ScmFile) it.next();

            System.out.println( " " + file.getPath() );
        }
    }

    private void update( ScmRepository scmRepository, File workingDirectory, String tag )
        throws ScmException
    {
        if ( !workingDirectory.exists() )
        {
            System.err.println( "The working directory doesn't exist: '" + workingDirectory.getAbsolutePath() + "'." );

            return;
        }

        UpdateScmResult result = scmManager.update( scmRepository, new ScmFileSet( workingDirectory ), tag );

        if ( !result.isSuccess() )
        {
            showError( result );

            return;
        }

        List updatedFiles = result.getUpdatedFiles();

        System.out.println( "Updated these files: " );

        for ( Iterator it = updatedFiles.iterator(); it.hasNext(); )
        {
            ScmFile file = (ScmFile) it.next();

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
