package org.apache.maven.scm.provider.cvslib.cvsjava.command.add;

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
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.cvslib.command.add.AbstractCvsAddCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.util.CvsConnection;
import org.apache.maven.scm.provider.cvslib.cvsjava.util.CvsLogListener;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsJavaAddCommand
    extends AbstractCvsAddCommand
{
    protected AddScmResult executeCvsCommand( Commandline cl, List addedFiles )
        throws ScmException
    {
        CvsLogListener logListener = new CvsLogListener();

        try
        {
            boolean isSuccess = CvsConnection.processCommand( cl.getArguments(),
                                                              cl.getWorkingDirectory().getAbsolutePath(), logListener,
                                                              getLogger() );

            // TODO: actually it may have partially succeeded - should we cvs update the files and parse "A " responses?
            if ( !isSuccess )
            {
                return new AddScmResult( cl.toString(), "The cvs command failed.", logListener.getStdout().toString(),
                                         false );
            }

            return new AddScmResult( cl.toString(), addedFiles );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return new AddScmResult( cl.toString(), "The cvs command failed.", logListener.getStdout().toString(),
                                     false );
        }
    }
}
