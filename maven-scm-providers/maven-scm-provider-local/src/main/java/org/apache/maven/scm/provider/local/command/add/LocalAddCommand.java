package org.apache.maven.scm.provider.local.command.add;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.LocalCommand;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;

import java.io.File;
import java.util.Collections;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class LocalAddCommand
    extends AbstractAddCommand
    implements LocalCommand
{
    protected ScmResult executeAddCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {
        LocalScmProviderRepository localRepo = (LocalScmProviderRepository) repository;

        File[] files = fileSet.getFiles();
        for ( int i = 0; i < files.length; i++ )
        {
            // TODO: better to standardise on relative paths inside fileset

            String path = files[i].getPath();
            if ( path.startsWith( fileSet.getBasedir().getPath() ) )
            {
                path = path.substring( fileSet.getBasedir().getPath().length() );
            }
            path = path.replace( '\\', '/' );

            if ( path.startsWith( "/" ) )
            {
                path = path.substring( 1 );
            }

            localRepo.addFile( path );
        }

        // TODO: Also, ensure it is tested from the update test
        return new AddScmResult( Collections.EMPTY_LIST );
    }
}
