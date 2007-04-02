package org.apache.maven.scm.provider.cvslib.command.checkin;

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
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractCvsCheckInCommand
    extends AbstractCheckInCommand
    implements CvsCommand
{
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      ScmVersion version )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "commit", repository, fileSet, false );

        if ( version != null && !StringUtils.isEmpty( version.getName() ) )
        {
            cl.createArgument().setValue( "-r" + version.getName() );
        }

        cl.createArgument().setValue( "-R" );

        cl.createArgument().setValue( "-F" );

        File messageFile;

        try
        {
            messageFile = File.createTempFile( "scm-commit-message", ".txt" );

            FileUtils.fileWrite( messageFile.getAbsolutePath(), message );
        }
        catch ( IOException ex )
        {
            throw new ScmException( "Error while making a temporary commit message file." );
        }

        cl.createArgument().setValue( messageFile.getAbsolutePath() );

        File[] files = fileSet.getFiles();

        for ( int i = 0; i < files.length; i++ )
        {
            cl.createArgument().setValue( files[i].getPath().replace( '\\', '/' ) );
        }

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        CheckInScmResult result = executeCvsCommand( cl, repository, messageFile );

        try
        {
            FileUtils.forceDelete( messageFile );
        }
        catch ( IOException ex )
        {
            // ignore
        }

        return result;
    }

    protected abstract CheckInScmResult executeCvsCommand( Commandline cl, CvsScmProviderRepository repository,
                                                           File messageFile )
        throws ScmException;
}
