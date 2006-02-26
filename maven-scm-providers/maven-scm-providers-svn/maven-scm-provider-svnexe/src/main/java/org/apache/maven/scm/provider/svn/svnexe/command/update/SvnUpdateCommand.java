package org.apache.maven.scm.provider.svn.svnexe.command.update;

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
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.update.SvnUpdateScmResult;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.apache.maven.scm.provider.svn.svnexe.command.changelog.SvnChangeLogCommand;
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

    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        Commandline cl = createCommandLine( (SvnScmProviderRepository) repo, fileSet.getBasedir(), tag );

        SvnUpdateConsumer consumer = new SvnUpdateConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Executing: " + cl );
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

        return new SvnUpdateScmResult( cl.toString(), consumer.getUpdatedFiles(), consumer.getRevision() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 String tag )
    {
        if ( tag != null && StringUtils.isEmpty( tag.trim() ) )
        {
            tag = null;
        }

        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory, repository );

        if ( tag == null || SvnTagBranchUtils.isRevisionSpecifier( tag ) )
        {
            cl.createArgument().setValue( "update" );

            if ( tag != null )
            {
                cl.createArgument().setValue( "-r" );
                cl.createArgument().setValue( tag );
            }
        }
        else
        {
            // The tag specified does not appear to be numeric, so assume it refers 
            // to a branch/tag url and perform a switch operation rather than update
            cl.createArgument().setValue( "switch" );
            cl.createArgument().setValue( SvnTagBranchUtils.resolveTagUrl( repository, tag ) );
            cl.createArgument().setValue( workingDirectory.getAbsolutePath() );
        }

        return cl;
    }

    /**
     * @see org.apache.maven.scm.command.update.AbstractUpdateCommand#getChangeLogCommand()
     */
    protected ChangeLogCommand getChangeLogCommand()
    {
        SvnChangeLogCommand command = new SvnChangeLogCommand();

        command.setLogger( getLogger() );

        return command;
    }


}
