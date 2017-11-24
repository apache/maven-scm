package org.apache.maven.scm.provider.svn.svnexe.command.checkout;

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
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnCommandUtils;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 *
 */
public class SvnCheckOutCommand
    extends AbstractCheckOutCommand
    implements SvnCommand
{
    /**
     * {@inheritDoc}
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                       ScmVersion version, boolean recursive, boolean shallow )
        throws ScmException
    {
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

        Commandline cl = createCommandLine( repository, fileSet.getBasedir(), version, url, recursive );

        SvnCheckOutConsumer consumer = new SvnCheckOutConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + SvnCommandLineUtils.cryptPassword( cl ) );

            if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
            {
                getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
            }
        }

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
            return new CheckOutScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new CheckOutScmResult( cl.toString(), Integer.toString( consumer.getRevision() ),
                                      consumer.getCheckedOutFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Create SVN check out command line in a recursive way.
     *
     * @param repository       not null
     * @param workingDirectory not null
     * @param version          not null
     * @param url              not null
     * @return the SVN command line for the SVN check out.
     * @see #createCommandLine(SvnScmProviderRepository, File, ScmVersion, String, boolean)
     */
    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 ScmVersion version, String url )
    {
        return createCommandLine( repository, workingDirectory, version, url, true );
    }

    /**
     * Create SVN check out command line.
     *
     * @param repository       not null
     * @param workingDirectory not null
     * @param version          not null
     * @param url              not null
     * @param recursive        <code>true</code> if recursive check out is wanted, <code>false</code> otherwise.
     * @return the SVN command line for the SVN check out.
     * @since 1.1.1
     */
    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 ScmVersion version, String url, boolean recursive )
    {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory.getParentFile(), repository );

        cl.createArg().setValue( "checkout" );

        // add non recursive option
        if ( !recursive )
        {
            cl.createArg().setValue( "-N" );
        }

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            if ( version instanceof ScmRevision )
            {
                cl.createArg().setValue( "-r" );

                cl.createArg().setValue( version.getName() );
            }
        }

        cl.createArg().setValue( url );

        cl.createArg().setValue( workingDirectory.getAbsolutePath() );

        return cl;
    }
}
