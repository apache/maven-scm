package org.apache.maven.scm.provider.starteam.command.add;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;

import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id: $
 */
public class StarteamAddCommand
    extends AbstractAddCommand
    implements StarteamCommand
{
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {

        //work around until maven-scm-api allow this
        String issue = System.getProperty( "maven.scm.issue" );

        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        StarteamAddConsumer consumer = new StarteamAddConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        File[] files = fileSet.getFiles();

        for ( int i = 0; i < files.length; ++i )
        {
            Commandline cl = createCommandLine( repository, files[i], issue );

            int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

            if ( exitCode != 0 )
            {
                return new AddScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(), false );
            }
        }

        return new AddScmResult( null, consumer.getAddedFiles() );
    }

    static Commandline createCommandLine( StarteamScmProviderRepository repo, File toBeAddedFile, String issue )
    {
        Commandline cl = StarteamCommandLineUtils.createStarteamBaseCommandLine( "add", toBeAddedFile, repo );

        if ( issue != null && issue.length() > 0 )
        {
            cl.createArgument().setValue( "-cr" );

            cl.createArgument().setValue( issue );
        }

        cl.createArgument().setValue( toBeAddedFile.getName() );

        return cl;
    }
}
