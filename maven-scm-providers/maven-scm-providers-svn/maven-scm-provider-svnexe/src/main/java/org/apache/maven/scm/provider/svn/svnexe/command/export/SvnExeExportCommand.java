package org.apache.maven.scm.provider.svn.svnexe.command.export;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.export.AbstractExportCommand;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.export.ExportScmResultWithRevision;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnCommandUtils;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.apache.maven.scm.provider.svn.svnexe.command.update.SvnUpdateConsumer;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class SvnExeExportCommand
    extends AbstractExportCommand
    implements SvnCommand
{
    /** {@inheritDoc} */
    protected ExportScmResult executeExportCommand( ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion version,
                                                    String outputDirectory )
        throws ScmException
    {

        if ( outputDirectory == null )
        {
            outputDirectory = fileSet.getBasedir().getAbsolutePath();
        }

        SvnScmProviderRepository repository = (SvnScmProviderRepository) repo;

        String url = repository.getUrl();

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            if ( version instanceof ScmTag )
            {
                url = SvnTagBranchUtils.resolveTagUrl( repository, (ScmTag) version );
            }
            else if ( version instanceof ScmBranch )
            {
                url = SvnTagBranchUtils.resolveBranchUrl( repository, (ScmBranch) version );
            }
        }

        url = SvnCommandUtils.fixUrl( url, repository.getUser() );

        Commandline cl =
            createCommandLine( (SvnScmProviderRepository) repo, fileSet.getBasedir(), version, url, outputDirectory );

        SvnUpdateConsumer consumer = new SvnUpdateConsumer( fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        if ( logger.isInfoEnabled() )
        {
            logger.info( "Executing: " + SvnCommandLineUtils.cryptPassword( cl ) );

            if ( cl.getWorkingDirectory() != null && Os.isFamily( Os.FAMILY_WINDOWS ) )
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

        if ( exitCode != 0 )
        {
            return new ExportScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new ExportScmResultWithRevision( cl.toString(), consumer.getUpdatedFiles(),
                                                String.valueOf( consumer.getRevision() ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 ScmVersion version, String url, String outputSirectory )
    {
        if ( version != null && StringUtils.isEmpty( version.getName() ) )
        {
            version = null;
        }

        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory, repository );

        cl.createArg().setValue( "export" );

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            if ( version instanceof ScmRevision )
            {
                cl.createArg().setValue( "-r" );

                cl.createArg().setValue( version.getName() );
            }
        }

        //support exporting to an existing directory
        cl.createArg().setValue( "--force" );

        cl.createArg().setValue( url + "@" );

        if ( StringUtils.isNotEmpty( outputSirectory ) )
        {
            cl.createArg().setValue( outputSirectory + "@" );
        }

        return cl;
    }
}
