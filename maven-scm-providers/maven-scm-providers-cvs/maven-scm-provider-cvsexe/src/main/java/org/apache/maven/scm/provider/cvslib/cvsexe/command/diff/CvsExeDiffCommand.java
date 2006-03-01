package org.apache.maven.scm.provider.cvslib.cvsexe.command.diff;

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
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.cvslib.command.diff.AbstractCvsDiffCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsExeDiffCommand
    extends AbstractCvsDiffCommand
{
    protected DiffScmResult executeCvsCommand( Commandline cl )
        throws ScmException
    {
        CvsDiffConsumer consumer = new CvsDiffConsumer( getLogger(), cl.getWorkingDirectory() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

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

        return new DiffScmResult( cl.toString(), consumer.getChangedFiles(), consumer.getDifferences(),
                                  consumer.getPatch() );

    }
}
