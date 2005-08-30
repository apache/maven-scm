package org.apache.maven.scm.provider.perforce.command.changelog;

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
import java.util.Date;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;

import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class PerforceChangeLogCommand
    extends AbstractChangeLogCommand
    implements PerforceCommand
{
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, int numDays, String branch )
        throws ScmException
    {
        if ( StringUtils.isNotEmpty( branch ) )
        {
            throw new ScmException( "This SCM doesn't support branches." );
        }

        Commandline cl = createCommandLine( (PerforceScmProviderRepository) repo, fileSet.getBasedir() );

        PerforceChangeLogConsumer consumer = new PerforceChangeLogConsumer( startDate, endDate );

        // TODO: implement

        return new ChangeLogScmResult( cl.toString(), consumer.getModifications() );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory )
    {
        Commandline command = new Commandline();

        command.setExecutable( "p4" );

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        if ( repo.getHost() != null )
        {
            command.createArgument().setValue( "-H" );

            String value = repo.getHost();

            if ( repo.getPort() != 0 )
            {
                value += ":" + Integer.toString( repo.getPort() );
            }

            command.createArgument().setValue( value );
        }

        if ( repo.getUser() != null )
        {
            command.createArgument().setValue( "-u" );

            command.createArgument().setValue( repo.getUser() );
        }

        if ( repo.getPassword() != null )
        {
            command.createArgument().setValue( "-P" );

            command.createArgument().setValue( repo.getPassword() );
        }

        command.createArgument().setValue( "filelog" );

        command.createArgument().setValue( "-t" );

        command.createArgument().setValue( "-l" );

        command.createArgument().setValue( repo.getPath() );

        return command;
    }
}
