package org.apache.maven.scm.provider.perforce.command.diff;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
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

/**
 * @todo refactor this & other perforce commands -- most of the invocation and stream
 *       consumer code could be shared
 * @author Mike Perham
 * @version $Id$
 */
public class PerforceDiffCommand
    extends AbstractDiffCommand
    implements PerforceCommand
{
    /** {@inheritDoc} */
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet files, ScmVersion startRev,
                                                ScmVersion endRev )
        throws ScmException
    {
        Commandline cl =
            createCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir(), startRev, endRev );
        PerforceDiffConsumer consumer = new PerforceDiffConsumer();
        getLogger().info( "Executing: " + PerforceScmProvider.clean( cl.toString() ) );
        boolean success = false;
        try
        {
            Process proc = cl.execute();
            // TODO find & use a less naive InputStream multiplexer
            BufferedReader stdout = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            BufferedReader stderr = new BufferedReader( new InputStreamReader( proc.getErrorStream() ) );
            String line;
            while ( ( line = stdout.readLine() ) != null )
            {
                getLogger().debug( "Consuming stdout: " + line );
                consumer.consumeLine( line );
            }
            while ( ( line = stderr.readLine() ) != null )
            {
                getLogger().debug( "Consuming stderr: " + line );
                consumer.consumeLine( line );
            }
            stderr.close();
            stdout.close();
            success = proc.waitFor() == 0;
        }
        catch ( CommandLineException e )
        {
            getLogger().error( e );
        }
        catch ( IOException e )
        {
            getLogger().error( e );
        }
        catch ( InterruptedException e )
        {
            getLogger().error( e );
        }

        return new DiffScmResult( cl.toString(), success ? "Diff successful" : "Unable to diff", consumer
            .getOutput(), success );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                 ScmVersion startRev, ScmVersion endRev )
    {
        String start = startRev != null && StringUtils.isNotEmpty( startRev.getName() ) ? "@" + startRev.getName() : "";
        String end = endRev != null && StringUtils.isNotEmpty( endRev.getName() ) ? endRev.getName() : "head";

        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArg().setValue( "diff2" );
        command.createArg().setValue( "-u" );
        // I'm assuming the "revs" are actually labels
        command.createArg().setValue( "..." + start );
        command.createArg().setValue( "...@" + end );
        return command;
    }

}
