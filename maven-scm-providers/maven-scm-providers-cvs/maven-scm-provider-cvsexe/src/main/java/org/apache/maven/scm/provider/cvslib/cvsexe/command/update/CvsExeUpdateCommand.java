package org.apache.maven.scm.provider.cvslib.cvsexe.command.update;

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
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.cvslib.command.update.AbstractCvsUpdateCommand;
import org.apache.maven.scm.provider.cvslib.command.update.CvsUpdateConsumer;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.changelog.CvsExeChangeLogCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsExeUpdateCommand
    extends AbstractCvsUpdateCommand
{
    protected UpdateScmResult executeCvsCommand( Commandline cl )
        throws ScmException
    {
        CvsUpdateConsumer consumer = new CvsUpdateConsumer( getLogger() );

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
            return new UpdateScmResult( cl.toString(), "The cvs command failed.", stderr.getOutput(), false );
        }

        return new UpdateScmResult( cl.toString(), consumer.getUpdatedFiles() );
    }

    protected ChangeLogCommand getChangeLogCommand()
    {
        CvsExeChangeLogCommand command = new CvsExeChangeLogCommand();

        command.setLogger( getLogger() );

        return command;
    }
}
