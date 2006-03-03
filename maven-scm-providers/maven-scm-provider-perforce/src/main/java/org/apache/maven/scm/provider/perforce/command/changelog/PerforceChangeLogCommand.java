package org.apache.maven.scm.provider.perforce.command.changelog;

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
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class PerforceChangeLogCommand
    extends AbstractChangeLogCommand
    implements PerforceCommand
{

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, String branch,
                                                          String datePattern )
        throws ScmException
    {
        if ( StringUtils.isNotEmpty( branch ) )
        {
            throw new ScmException( "This SCM doesn't support branches." );
        }

        PerforceScmProviderRepository p4repo = (PerforceScmProviderRepository) repo;
        String clientspec = System.getProperty( PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY );
        Commandline cl = createCommandLine( p4repo, fileSet.getBasedir(), clientspec );

        PerforceChangeLogConsumer consumer =
            new PerforceChangeLogConsumer( p4repo.getPath(), startDate, endDate, datePattern, getLogger() );

        try
        {
            getLogger().debug( PerforceScmProvider.clean( "Executing " + cl.toString() ) );
            Process proc = cl.execute();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                consumer.consumeLine( line );
            }
        }
        catch ( CommandLineException e )
        {
            getLogger().error( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            getLogger().error( e.getMessage(), e );
        }

        return new ChangeLogScmResult( cl.toString(),
                                       new ChangeLogSet( consumer.getModifications(), startDate, endDate ) );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                 String clientspec )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        if ( clientspec != null )
        {
            command.createArgument().setValue( "-c" );
            command.createArgument().setValue( clientspec );
        }
        command.createArgument().setValue( "filelog" );
        command.createArgument().setValue( "-t" );
        command.createArgument().setValue( "-l" );
        command.createArgument().setValue( "..." );

        return command;
    }
}
