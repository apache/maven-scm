package org.apache.maven.scm.provider.cvslib.command.diff;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class CvsDiffCommand
    extends AbstractDiffCommand
    implements CvsCommand
{
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet, String startRevision,
                                                String endRevision )
        throws ScmException
    {
        Commandline cl = createCommandLine( fileSet.getBasedir(), startRevision, endRevision );

        CvsDiffConsumer consumer = new CvsDiffConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        int exitCode;

        getLogger().debug( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );
        getLogger().debug( "Command line: " + cl );
        
        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

// TODO: a difference returns a code of "1", as does errors. How to tell the difference?
//        if ( exitCode != 0 )
//        {
//            return new DiffScmResult( cl.toString(), "The cvs command failed.", stderr.getOutput(), false );
//        }

        return new DiffScmResult( cl.toString(), consumer.getChangedFiles(), consumer.getDifferences(), consumer.getPatch() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( File workingDirectory, String startRevision, String endRevision )
    {
        Commandline cl = new Commandline();

        cl.setExecutable( "cvs" );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        cl.createArgument().setValue( "-q" );

        cl.createArgument().setValue( "-f" ); // don't use ~/.cvsrc

        cl.createArgument().setValue( "diff" );

        cl.createArgument().setValue( "-u" );

        cl.createArgument().setValue( "-N" );

        if ( startRevision != null )
        {
            cl.createArgument().setValue( "-r" );
            cl.createArgument().setValue( startRevision );
        }

        if ( endRevision != null )
        {
            cl.createArgument().setValue( "-r" );
            cl.createArgument().setValue( endRevision );
        }

        return cl;
    }
}
