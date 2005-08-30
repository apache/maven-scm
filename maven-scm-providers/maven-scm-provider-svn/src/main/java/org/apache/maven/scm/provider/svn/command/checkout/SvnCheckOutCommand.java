package org.apache.maven.scm.provider.svn.command.checkout;

/*
 * Copyright 2003-2005 The Apache Software Foundation.
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
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.SvnCommandLineUtils;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnCheckOutCommand
    extends AbstractCheckOutCommand
    implements SvnCommand
{
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        SvnScmProviderRepository repository = (SvnScmProviderRepository) repo;

        String url = repository.getUrl();
        if ( tag != null )
        {
            String tagBase = repository.getTagBase();

            if ( tagBase == null )
            {
                throw new ScmException( "tag base must be specified" );
            }
            url = tagBase + "/" + tag;
        }

        // TODO: revision
        Commandline cl = createCommandLine( repository, fileSet.getBasedir(), null, url );

        SvnCheckOutConsumer consumer = new SvnCheckOutConsumer( getLogger(), fileSet.getBasedir().getParentFile() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );
        getLogger().info( "Command line: " + cl );

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new CheckOutScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new CheckOutScmResult( cl.toString(), consumer.getCheckedOutFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 String revision, String url )
    {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory.getParentFile(), repository );

        cl.createArgument().setValue( "checkout" );

        if ( revision != null )
        {
            cl.createArgument().setValue( "-r" );

            cl.createArgument().setValue( revision );
        }

        cl.createArgument().setValue( url );

        cl.createArgument().setValue( workingDirectory.getName() );

        return cl;
    }
}
