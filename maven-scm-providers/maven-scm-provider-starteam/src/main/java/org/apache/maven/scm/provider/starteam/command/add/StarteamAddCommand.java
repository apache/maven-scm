package org.apache.maven.scm.provider.starteam.command.add;

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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 *
 */
public class StarteamAddCommand
    extends AbstractAddCommand
    implements StarteamCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {

        //work around until maven-scm-api allow this
        String issue = System.getProperty( "maven.scm.issue" );

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );
        }

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        StarteamAddConsumer consumer = new StarteamAddConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        for ( File fileToBeAdded : fileSet.getFileList() )
        {
            ScmFileSet scmFile = new ScmFileSet( fileSet.getBasedir(), fileToBeAdded );

            Commandline cl = createCommandLine( repository, scmFile, issue );

            int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

            if ( exitCode != 0 )
            {
                return new AddScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(), false );
            }
        }

        return new AddScmResult( null, consumer.getAddedFiles() );
    }

    static Commandline createCommandLine( StarteamScmProviderRepository repo, ScmFileSet scmFileSet, String issue )
    {
        List<String> args = new ArrayList<String>();

        if ( issue != null && issue.length() != 0 )
        {
            args.add( "-cr" );
            args.add( issue );
        }

        StarteamCommandLineUtils.addEOLOption( args );

        return StarteamCommandLineUtils.createStarteamCommandLine( "add", args, scmFileSet, repo );
    }
}
