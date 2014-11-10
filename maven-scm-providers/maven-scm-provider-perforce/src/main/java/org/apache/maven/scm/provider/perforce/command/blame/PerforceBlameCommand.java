package org.apache.maven.scm.provider.perforce.command.blame;

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
import org.apache.maven.scm.command.blame.AbstractBlameCommand;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 * @author Olivier Lamy
 * @since 1.4
 */
public class PerforceBlameCommand
    extends AbstractBlameCommand
    implements PerforceCommand
{
    public BlameScmResult executeBlameCommand( ScmProviderRepository repo, ScmFileSet workingDirectory,
                                               String filename )
        throws ScmException
    {
        // Call annotate command
        PerforceScmProviderRepository p4repo = (PerforceScmProviderRepository) repo;
        String clientspec = PerforceScmProvider.getClientspecName( getLogger(), p4repo, workingDirectory.getBasedir() );
        Commandline cl =
            createCommandLine( (PerforceScmProviderRepository) repo, workingDirectory.getBasedir(), filename,
                               clientspec );

        PerforceBlameConsumer blameConsumer = new PerforceBlameConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, blameConsumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }
        if ( exitCode != 0 )
        {
            return new BlameScmResult( cl.toString(), "The perforce command failed.", stderr.getOutput(), false );
        }

        // Call filelog command

        cl =
            createFilelogCommandLine( (PerforceScmProviderRepository) repo, workingDirectory.getBasedir(), filename,
                                      clientspec );

        PerforceFilelogConsumer filelogConsumer = new PerforceFilelogConsumer( getLogger() );

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, filelogConsumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }
        if ( exitCode != 0 )
        {
            return new BlameScmResult( cl.toString(), "The perforce command failed.", stderr.getOutput(), false );
        }

        // Combine results

        List<BlameLine> lines = blameConsumer.getLines();
        for ( int i = 0; i < lines.size(); i++ )
        {
            BlameLine line = lines.get( i );
            String revision = line.getRevision();
            line.setAuthor( filelogConsumer.getAuthor( revision ) );
            line.setDate( filelogConsumer.getDate( revision ) );
        }

        return new BlameScmResult( cl.toString(), lines );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                 String filename, final String clientspec )
    {
        Commandline cl = PerforceScmProvider.createP4Command( repo, workingDirectory );
        if ( clientspec != null )
        {
            cl.createArg().setValue( "-c" );
            cl.createArg().setValue( clientspec );
        }        
        cl.createArg().setValue( "annotate" );
        cl.createArg().setValue( "-q" ); // quiet
        cl.createArg().setValue( filename );
        return cl;
    }

    public static Commandline createFilelogCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                        String filename, final String clientspec )
    {
        Commandline cl = PerforceScmProvider.createP4Command( repo, workingDirectory );
        if ( clientspec != null )
        {
            cl.createArg().setValue( "-c" );
            cl.createArg().setValue( clientspec );
        }
        cl.createArg().setValue( "filelog" );
        cl.createArg().setValue( filename );
        return cl;
    }
}
