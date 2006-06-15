package org.apache.maven.scm.provider.perforce.command.diff;

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
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogCommand.java 264804 2005-08-30 16:09:04Z
 *          evenisse $
 */
public class PerforceDiffCommand
    extends AbstractDiffCommand
    implements PerforceCommand
{

    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet files, String startRev,
                                                String endRev )
        throws ScmException
    {
        Commandline cl =
            createCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir(), startRev, endRev );
        PerforceDiffConsumer consumer = new PerforceDiffConsumer();
        boolean success = false;
        try
        {
            Process proc = cl.execute();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                consumer.consumeLine( line );
            }
            success = proc.waitFor() == 0;
        }
        catch ( CommandLineException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }

        return new DiffScmResult( cl.toString(), success ? "Diff successful" : "Unable to diff", consumer
            .getOutput(), success );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                 String startRev, String endRev )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArgument().setValue( "diff2" );
        command.createArgument().setValue( "-u" );
        // I'm assuming the "revs" are actually labels
        command.createArgument().setValue( "..." + ( startRev != null ? "@" + startRev : "" ) );
        command.createArgument().setValue( "...@" + ( endRev != null ? endRev : "head" ) );
        return command;
    }

}