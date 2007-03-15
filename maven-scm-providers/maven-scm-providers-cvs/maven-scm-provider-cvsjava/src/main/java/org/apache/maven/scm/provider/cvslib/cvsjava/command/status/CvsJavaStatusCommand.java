package org.apache.maven.scm.provider.cvslib.cvsjava.command.status;

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
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.cvslib.command.status.AbstractCvsStatusCommand;
import org.apache.maven.scm.provider.cvslib.command.status.CvsStatusConsumer;
import org.apache.maven.scm.provider.cvslib.cvsjava.util.CvsConnection;
import org.apache.maven.scm.provider.cvslib.cvsjava.util.CvsLogListener;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsJavaStatusCommand
    extends AbstractCvsStatusCommand
{
    protected StatusScmResult executeCvsCommand( Commandline cl )
        throws ScmException
    {
        CvsLogListener logListener = new CvsLogListener();

        CvsStatusConsumer consumer = new CvsStatusConsumer( getLogger(), cl.getWorkingDirectory() );

        try
        {
            boolean isSuccess = CvsConnection.processCommand( cl.getArguments(),
                                                              cl.getWorkingDirectory().getAbsolutePath(), logListener,
                                                              getLogger() );

            if ( !isSuccess )
            {
                return new StatusScmResult( cl.toString(), "The cvs command failed.",
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
            return new StatusScmResult( cl.toString(), "The cvs command failed.", logListener.getStderr().toString(),
                                        false );
        }

        return new StatusScmResult( cl.toString(), consumer.getChangedFiles() );
    }
}
