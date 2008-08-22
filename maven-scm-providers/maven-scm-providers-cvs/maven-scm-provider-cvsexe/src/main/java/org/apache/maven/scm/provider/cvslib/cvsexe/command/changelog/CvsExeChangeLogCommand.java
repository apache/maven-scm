package org.apache.maven.scm.provider.cvslib.cvsexe.command.changelog;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.cvslib.command.changelog.AbstractCvsChangeLogCommand;
import org.apache.maven.scm.provider.cvslib.command.changelog.CvsChangeLogConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsExeChangeLogCommand
    extends AbstractCvsChangeLogCommand
{
    /** {@inheritDoc} */
    protected ChangeLogScmResult executeCvsCommand( Commandline cl, Date startDate, Date endDate,
                                                    ScmVersion startVersion, ScmVersion endVersion, String datePattern )
        throws ScmException
    {
        CvsChangeLogConsumer consumer = new CvsChangeLogConsumer( getLogger(), datePattern );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing cvs command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new ChangeLogScmResult( cl.toString(), "The cvs command failed.", stderr.getOutput(), false );
        }

        ChangeLogSet changeLogSet = new ChangeLogSet( consumer.getModifications(), startDate, endDate );
        changeLogSet.setStartVersion( startVersion );
        changeLogSet.setEndVersion( endVersion );

        return new ChangeLogScmResult( cl.toString(), changeLogSet );
    }
}
