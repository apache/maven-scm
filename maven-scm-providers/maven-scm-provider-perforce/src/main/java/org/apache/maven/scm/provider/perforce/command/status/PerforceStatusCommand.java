package org.apache.maven.scm.provider.perforce.command.status;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.command.PerforceVerbMapper;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.regexp.RE;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogCommand.java 264804 2005-08-30 16:09:04Z
 *          evenisse $
 */
public class PerforceStatusCommand
    extends AbstractStatusCommand
    implements PerforceCommand
{

    protected StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet files )
        throws ScmException
    {
        PerforceScmProviderRepository prepo = (PerforceScmProviderRepository) repo;
        PerforceStatusConsumer consumer = new PerforceStatusConsumer();
        Commandline command = readOpened( prepo, files, consumer );

        if ( consumer.isSuccess() )
        {
            List scmfiles = createResults( prepo.getPath(), consumer );
            return new StatusScmResult( command.toString(), scmfiles );
        }
        else
        {
            return new StatusScmResult( command.toString(), "Unable to get status", consumer
                .getOutput(), consumer.isSuccess() );
        }
    }

    public static List createResults( String repoPath, PerforceStatusConsumer consumer )
    {
        List results = new ArrayList();
        List files = consumer.getDepotfiles();
        String root = repoPath;
        RE re = new RE( "([^#]+)#\\d+ - ([^ ]+) .*" );
        for ( Iterator it = files.iterator(); it.hasNext(); )
        {
            String filepath = (String) it.next();
            if ( !re.match( filepath ) )
            {
                System.err.println( "Skipping " + filepath );
                continue;
            }
            String path = re.getParen( 1 );
            String verb = re.getParen( 2 );

            ScmFile scmfile = new ScmFile( path.substring( root.length() + 1 ).trim(), PerforceVerbMapper
                .toStatus( verb ) );
            results.add( scmfile );
        }
        return results;
    }

    private Commandline readOpened( PerforceScmProviderRepository prepo, ScmFileSet files, PerforceStatusConsumer consumer )
    {
        Commandline cl = createOpenedCommandLine( prepo, files.getBasedir() );
        try
        {
            getLogger().debug( PerforceScmProvider.clean( "Executing " + cl.toString() ) );
            Process proc = cl.execute();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line = null;
            while ( ( line = br.readLine() ) != null )
            {
                getLogger().debug("Reading " + line);
                consumer.consumeLine( line );
            }
            br.close();
        }
        catch ( CommandLineException e )
        {
            getLogger().error(e);
        }
        catch ( IOException e )
        {
            getLogger().error(e);
        }
        return cl;
    }

    public static Commandline createOpenedCommandLine( PerforceScmProviderRepository repo, File workingDirectory )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );
        command.createArgument().setValue( "opened" );
        command.createArgument().setValue( PerforceScmProvider.getCanonicalRepoPath( repo.getPath() ) );
        return command;
    }
}
