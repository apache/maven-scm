package org.apache.maven.scm.provider.svn.svnexe.command.list;

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
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.Iterator;

/**
 * Command to list files in SVN ( <code>svn list</code> command )
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 *
 */
public class SvnListCommand
    extends AbstractListCommand
    implements SvnCommand
{
    private static final File TMP_DIR = new File( System.getProperty( "java.io.tmpdir" ) );

    /** {@inheritDoc} */
    protected ListScmResult executeListCommand( ScmProviderRepository repository, ScmFileSet fileSet, boolean recursive,
                                                ScmVersion version )
        throws ScmException
    {
        Commandline cl = createCommandLine( (SvnScmProviderRepository) repository, fileSet, recursive, version );

        SvnListConsumer consumer = new SvnListConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + SvnCommandLineUtils.cryptPassword( cl ) );

            if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
            {
                getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
            }
        }

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
            return new ListScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new ListScmResult( cl.toString(), consumer.getFiles() );
    }

    static Commandline createCommandLine( SvnScmProviderRepository repository, ScmFileSet fileSet, boolean recursive,
                                          ScmVersion version )
    {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( TMP_DIR, repository );

        cl.createArg().setValue( "list" );

        if ( recursive )
        {
            cl.createArg().setValue( "--recursive" );
        }

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            if ( version instanceof ScmRevision )
            {
                cl.createArg().setValue( "-r" );

                cl.createArg().setValue( version.getName() );
            }
        }

        Iterator<File> it = fileSet.getFileList().iterator();

        while ( it.hasNext() )
        {
            File file = it.next();

            cl.createArg().setValue( repository.getUrl() + "/" + file.getPath().replace( '\\', '/' ) + "@" );
        }

        return cl;
    }

}
