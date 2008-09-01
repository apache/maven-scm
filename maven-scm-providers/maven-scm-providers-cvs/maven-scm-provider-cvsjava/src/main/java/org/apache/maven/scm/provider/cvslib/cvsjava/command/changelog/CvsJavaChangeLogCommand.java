package org.apache.maven.scm.provider.cvslib.cvsjava.command.changelog;

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
import org.apache.maven.scm.provider.cvslib.cvsjava.util.CvsConnection;
import org.apache.maven.scm.provider.cvslib.cvsjava.util.CvsLogListener;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsJavaChangeLogCommand
    extends AbstractCvsChangeLogCommand
{
    /** {@inheritDoc} */
    protected ChangeLogScmResult executeCvsCommand( Commandline cl, Date startDate, Date endDate,
                                                    ScmVersion startVersion, ScmVersion endVersion, String datePattern )
        throws ScmException
    {
        CvsLogListener logListener = new CvsLogListener();

        CvsChangeLogConsumer consumer = new CvsChangeLogConsumer( getLogger(), datePattern );

        try
        {
            boolean isSuccess = CvsConnection.processCommand( cl.getArguments(),
                                                              cl.getWorkingDirectory().getAbsolutePath(), logListener,
                                                              getLogger() );

            if ( !isSuccess )
            {
                return new ChangeLogScmResult( cl.toString(), "The cvs command failed.",
                                               logListener.getStderr().toString(), false );
            }
            BufferedReader stream = new BufferedReader(
                new InputStreamReader( new ByteArrayInputStream( logListener.getStdout().toString().getBytes() ) ) );

            String line;

            while ( ( line = stream.readLine() ) != null )
            {
                consumer.consumeLine( line );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return new ChangeLogScmResult( cl.toString(), "The cvs command failed.", logListener.getStdout().toString(),
                                           false );
        }

        ChangeLogSet changeLogSet = new ChangeLogSet( consumer.getModifications(), startDate, endDate );
        changeLogSet.setStartVersion( startVersion );
        changeLogSet.setEndVersion( endVersion );

        return new ChangeLogScmResult( cl.toString(), changeLogSet );
    }

    protected void addDateRangeParameter( Commandline cl, String dateRange )
    {
        cl.createArg().setValue( dateRange );
    }
}
