package org.apache.maven.scm.provider.svn.svnexe.command.checkin;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author Olivier Lamy
 *
 */
public class SvnCheckInCommand
    extends AbstractCheckInCommand
    implements SvnCommand
{
    /** {@inheritDoc} */
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      ScmVersion version )
        throws ScmException
    {
        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            throw new ScmException( "This provider command can't handle tags." );
        }

        File messageFile = FileUtils.createTempFile( "maven-scm-", ".commit", null );

        try
        {
            FileUtils.fileWrite( messageFile.getAbsolutePath(), "UTF-8", message );
        }
        catch ( IOException ex )
        {
            return new CheckInScmResult( null, "Error while making a temporary file for the commit message: "
                + ex.getMessage(), null, false );
        }

        Commandline cl = createCommandLine( (SvnScmProviderRepository) repo, fileSet, messageFile );

        SvnCheckInConsumer consumer = new SvnCheckInConsumer( fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        if ( logger.isInfoEnabled() )
        {
            logger.info( "Executing: " + SvnCommandLineUtils.cryptPassword( cl ) );

            if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
            {
                logger.info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
            }
        }

        int exitCode;

        try
        {
            exitCode = SvnCommandLineUtils.execute( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( messageFile );
            }
            catch ( IOException ex )
            {
                // ignore
            }
        }

        if ( exitCode != 0 )
        {
            return new CheckInScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new CheckInScmResult( cl.toString(), consumer.getCheckedInFiles(),
                                     Integer.toString( consumer.getRevision() ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, ScmFileSet fileSet,
                                                 File messageFile )
        throws ScmException
    {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( fileSet.getBasedir(), repository );

        cl.createArg().setValue( "commit" );

        cl.createArg().setValue( "--file" );

        cl.createArg().setValue( messageFile.getAbsolutePath() );

        cl.createArg().setValue( "--encoding" );

        cl.createArg().setValue( "UTF-8" );

        try
        {
            SvnCommandLineUtils.addTarget( cl, fileSet.getFileList() );
        }
        catch ( IOException e )
        {
            throw new ScmException( "Can't create the targets file", e );
        }

        return cl;
    }
}
