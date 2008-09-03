package org.apache.maven.scm.provider.starteam.command.remove;

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
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.command.checkin.StarteamCheckInConsumer;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamRemoveCommand
    extends AbstractRemoveCommand
    implements StarteamCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeRemoveCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message )
        throws ScmException
    {
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );
        }

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        StarteamCheckInConsumer consumer = new StarteamCheckInConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        List remvoveFiles = fileSet.getFileList();

        if ( remvoveFiles.size() == 0 )
        {
            Commandline cl = createCommandLine( repository, fileSet );

            int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

            if ( exitCode != 0 )
            {
                return new RemoveScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(), false );
            }
        }
        else
        {
            //update only interested files already on the local disk
            for ( int i = 0; i < remvoveFiles.size(); ++i )
            {
                File fileToBeRemoved = (File) remvoveFiles.get( i );
                ScmFileSet scmFileSet = new ScmFileSet( fileSet.getBasedir(), fileToBeRemoved );
                Commandline cl = createCommandLine( repository, scmFileSet );

                int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

                if ( exitCode != 0 )
                {
                    return new RemoveScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(),
                                                false );
                }
            }
        }

        return new RemoveScmResult( null, consumer.getCheckedInFiles() );

    }

    public static Commandline createCommandLine( StarteamScmProviderRepository repo, ScmFileSet dirOrFile )
    {
        return StarteamCommandLineUtils.createStarteamCommandLine( "remove", null, dirOrFile, repo );
    }
}
