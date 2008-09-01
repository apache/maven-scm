package org.apache.maven.scm.provider.svn.svnexe.command.update;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.update.UpdateScmResultWithRevision;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.apache.maven.scm.provider.svn.svnexe.command.changelog.SvnChangeLogCommand;
import org.apache.maven.scm.provider.svn.util.SvnUtil;
import org.apache.maven.scm.providers.svn.settings.Settings;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnUpdateCommand
    extends AbstractUpdateCommand
    implements SvnCommand
{
    /** {@inheritDoc} */
    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        Commandline cl = createCommandLine( (SvnScmProviderRepository) repo, fileSet.getBasedir(), version );

        SvnUpdateConsumer consumer = new SvnUpdateConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Executing: " + SvnCommandLineUtils.cryptPassword( cl ) );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        int exitCode;

        try
        {
            exitCode = SvnCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new UpdateScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new UpdateScmResultWithRevision( cl.toString(), consumer.getUpdatedFiles(),
                                                String.valueOf( consumer.getRevision() ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 ScmVersion version )
    {
        Settings settings = SvnUtil.getSettings();

        String workingDir = workingDirectory.getAbsolutePath();

        if ( settings.isUseCygwinPath() )
        {
            workingDir = settings.getCygwinMountPath() + "/" + workingDir;
            workingDir = StringUtils.replace( workingDir, ":", "" );
            workingDir = StringUtils.replace( workingDir, "\\", "/" );
        }

        if ( version != null && StringUtils.isEmpty( version.getName() ) )
        {
            version = null;
        }

        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory, repository );

        if ( version == null || SvnTagBranchUtils.isRevisionSpecifier( version ) )
        {
            cl.createArg().setValue( "update" );

            if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
            {
                cl.createArg().setValue( "-r" );
                cl.createArg().setValue( version.getName() );
            }

            cl.createArg().setValue( workingDir );
        }
        else
        {
            if ( version instanceof ScmBranch )
            {
                // The tag specified does not appear to be numeric, so assume it refers
                // to a branch/tag url and perform a switch operation rather than update
                cl.createArg().setValue( "switch" );
                if ( version instanceof ScmTag )
                {
                    cl.createArg().setValue( SvnTagBranchUtils.resolveTagUrl( repository, (ScmTag) version ) );
                }
                else
                {
                    cl.createArg().setValue(
                        SvnTagBranchUtils.resolveBranchUrl( repository, (ScmBranch) version ) );
                }
                cl.createArg().setValue( workingDir );
            }
        }

        return cl;
    }

    /** {@inheritDoc} */
    protected ChangeLogCommand getChangeLogCommand()
    {
        SvnChangeLogCommand command = new SvnChangeLogCommand();

        command.setLogger( getLogger() );

        return command;
    }


}
