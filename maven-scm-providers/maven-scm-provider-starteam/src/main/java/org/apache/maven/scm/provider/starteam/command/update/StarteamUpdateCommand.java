package org.apache.maven.scm.provider.starteam.command.update;

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
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.command.changelog.StarteamChangeLogCommand;
import org.apache.maven.scm.provider.starteam.command.checkout.StarteamCheckOutConsumer;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamUpdateCommand
    extends AbstractUpdateCommand
    implements StarteamCommand
{
    // ----------------------------------------------------------------------
    // AbstractUpdateCommand Implementation
    // ----------------------------------------------------------------------

    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {

        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        StarteamCheckOutConsumer consumer = new StarteamCheckOutConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        List updateFiles = fileSet.getFileList();

        if ( updateFiles.size() == 0 )
        {
            //update everything
            Commandline cl = createCommandLine( repository, fileSet, tag );

            int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

            if ( exitCode != 0 )
            {
                return new UpdateScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(), false );
            }
            else
            {
                //hiden feature to allow Continuous Integration machine to
                // delete local files. It affectively remove all build ouput as well
                String doDeleteLocal = System.getProperty( "maven.scm.starteam.deleteLocal" );

                if ( "true".equalsIgnoreCase( doDeleteLocal ) )
                {
                    this.deleteLocal( repository, fileSet.getBasedir(), tag );
                }
            }
        }
        else
        {
            //update only interested files already on the local disk
            for ( int i = 0; i < updateFiles.size(); ++i )
            {
            	File updateFile = (File) updateFiles.get( i );
            	ScmFileSet scmFileSet = new ScmFileSet( fileSet.getBasedir(), updateFile );
                Commandline cl = createCommandLine( repository, scmFileSet, tag );

                int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

                if ( exitCode != 0 )
                {
                    return new UpdateScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(),
                                                false );
                }
            }
        }

        return new UpdateScmResult( null, consumer.getCheckedOutFiles() );

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( StarteamScmProviderRepository repo, ScmFileSet fileSet, String tag )
    {
        Commandline cl = StarteamCommandLineUtils.createStarteamBaseCommandLine( "co", fileSet, repo );

        cl.createArgument().setValue( "-merge" );

        cl.createArgument().setValue( "-neverprompt" );

        if ( tag != null && tag.length() != 0 )
        {
            cl.createArgument().setValue( "-vl" );

            cl.createArgument().setValue( tag );
        }

        if ( fileSet.getFileList().size() == 0 )
        {
            cl.createArgument().setValue( "-is" );
        }
        else
        {
        	File updateFile = (File) fileSet.getFileList().get( 0 );
            cl.createArgument().setValue( updateFile.getName() );
        }

        return cl;
    }

    /**
     * @see org.apache.maven.scm.command.update.AbstractUpdateCommand#getChangeLogCommand()
     */
    protected ChangeLogCommand getChangeLogCommand()
    {
        StarteamChangeLogCommand command = new StarteamChangeLogCommand();

        command.setLogger( getLogger() );

        return command;
    }

    private void deleteLocal( StarteamScmProviderRepository repo, File dir, String tag )
        throws ScmException
    {
        if ( dir.isFile() )
        {
            return;
        }

        Commandline cl = createDeleteLocalCommand( repo, dir, tag );

        StreamConsumer consumer = new DefaultConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

        if ( exitCode != 0 )
        {
            throw new ScmException( "Error executing delete-local: " + stderr.toString() );
        }
    }

    public static Commandline createDeleteLocalCommand( StarteamScmProviderRepository repo, File dirOrFile, String tag )
    {
        Commandline cl = StarteamCommandLineUtils.createStarteamBaseCommandLine( "delete-local", dirOrFile, repo );

        if ( dirOrFile.isDirectory() )
        {
            cl.createArgument().setValue( "-is" );
        }

        if ( tag != null && tag.length() != 0 )
        {
            cl.createArgument().setValue( "-cfgl " );

            cl.createArgument().setValue( tag );
        }

        cl.createArgument().setValue( "-filter" );

        cl.createArgument().setValue( "N" );

        return cl;
    }

}
