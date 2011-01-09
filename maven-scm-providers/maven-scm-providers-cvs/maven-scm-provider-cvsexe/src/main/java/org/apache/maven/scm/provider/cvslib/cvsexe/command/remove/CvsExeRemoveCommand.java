package org.apache.maven.scm.provider.cvslib.cvsexe.command.remove;

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

import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.cvslib.command.remove.AbstractCvsRemoveCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class CvsExeRemoveCommand
    extends AbstractCvsRemoveCommand
{
    /** {@inheritDoc} */
    protected RemoveScmResult executeCvsCommand( Commandline cl, List<ScmFile> removedFiles )
        throws ScmException
    {
        CommandLineUtils.StringStreamConsumer consumer = new CommandLineUtils.StringStreamConsumer();

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

        // TODO: actually it may have partially succeeded - should we cvs update the files and parse "A " responses?
        if ( exitCode != 0 )
        {
            return new RemoveScmResult( cl.toString(), "The cvs command failed.", stderr.getOutput(), false );
        }

        return new RemoveScmResult( cl.toString(), removedFiles );
    }
}
