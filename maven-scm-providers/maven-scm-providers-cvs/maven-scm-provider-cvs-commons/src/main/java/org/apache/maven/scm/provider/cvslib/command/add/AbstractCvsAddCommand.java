package org.apache.maven.scm.provider.cvslib.command.add;

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
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @todo separate the CVSlib stuff from the cvs command line so it is clear what needs to be updated eventually
 */
public abstract class AbstractCvsAddCommand
    extends AbstractAddCommand
    implements CvsCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "add", repository, fileSet );

        if ( binary )
        {
            cl.createArgument().setValue( "-kb" );
        }

        if ( message != null && message.length() > 0 )
        {
            cl.createArgument().setValue( "-m" );

            cl.createArgument().setValue( "\"" + message + "\"" );
        }

        File[] files = fileSet.getFiles();

        List addedFiles = new ArrayList();

        for ( int i = 0; i < files.length; i++ )
        {
            String path = files[i].getPath().replace( '\\', '/' );

            cl.createArgument().setValue( path );

            addedFiles.add( new ScmFile( path, ScmFileStatus.ADDED ) );
        }

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        return executeCvsCommand( cl, addedFiles );
    }

    protected abstract AddScmResult executeCvsCommand( Commandline cl, List/*<ScmFile>*/ addedFiles )
        throws ScmException;
}
