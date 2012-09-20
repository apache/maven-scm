package org.apache.maven.scm.provider.cvslib.cvsexe.command.list;

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
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.cvslib.command.list.AbstractCvsListCommand;
import org.apache.maven.scm.provider.cvslib.command.status.CvsStatusConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 *
 */
public class CvsExeListCommand
    extends AbstractCvsListCommand
{
    /** {@inheritDoc} */
    protected ListScmResult executeCvsCommand( Commandline cl )
        throws ScmException
    {
        CvsStatusConsumer consumer = new CvsStatusConsumer( getLogger(), cl.getWorkingDirectory() );

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

        if ( exitCode != 0 )
        {
            return new ListScmResult( cl.toString(), "The cvs command failed.", stderr.getOutput(), false );
        }

        return new ListScmResult( cl.toString(), consumer.getChangedFiles() );

    }
}
