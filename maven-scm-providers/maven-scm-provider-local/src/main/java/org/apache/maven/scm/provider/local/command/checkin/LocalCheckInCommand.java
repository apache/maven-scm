package org.apache.maven.scm.provider.local.command.checkin;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.LocalCommand;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LocalCheckInCommand
    extends AbstractCheckInCommand
    implements LocalCommand
{
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, File workingDirectory, String message, String tag, File[] files )
        throws ScmException
    {
        LocalScmProviderRepository repository = (LocalScmProviderRepository) repo;

        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "The local scm doesn't support tags." );
        }

        File root = new File( repository.getRoot() );

        String module = repository.getModule();

        File source = new File( root, module );

        File baseDestination = workingDirectory;

        if ( !workingDirectory.exists() )
        {
            throw new ScmException( "The working directory doesn't exist (" + workingDirectory.getAbsolutePath() + ")." );
        }

        if ( !root.exists() )
        {
            throw new ScmException( "The base directory doesn't exist (" + root.getAbsolutePath() + ")." );
        }

        if ( !source.exists() )
        {
            throw new ScmException( "The module directory doesn't exist (" + source.getAbsolutePath() + ")." );
        }

    	List checkedInFiles = new ArrayList();

        try
        {
            // Only copy files newer than in the repo
            File repoRoot = new File( repository.getRoot(), repository.getModule() );

            Iterator it = FileUtils.getFiles( workingDirectory, "**", null ).iterator();

            while ( it.hasNext() )
            {
                File file = (File) it.next();

                String path = file.getAbsolutePath().substring( workingDirectory.getAbsolutePath().length());

                File repoFile = new File( repoRoot, path );

                ScmFileStatus status;

                if ( repoFile.exists() )
                {
                    String repoFileContents = FileUtils.fileRead( repoFile );

                    String fileContents = FileUtils.fileRead( file );

                    System.err.println("fileContents:" + fileContents);
                    System.err.println("repoFileContents:" + repoFileContents);
                    if ( fileContents.equals( repoFileContents ) )
                    {
                        continue;
                    }

                    status = ScmFileStatus.CHECKED_IN;
                }
                else
                {
                    status = ScmFileStatus.ADDED;
                }

                FileUtils.copyFile( file, repoFile );

                System.err.println(new ScmFile( path, status ));
                checkedInFiles.add( new ScmFile( path, status ) );
            }
        }
        catch( IOException ex )
        {
            throw new ScmException( "Error while checking in the files.", ex );
        }

        return new CheckInScmResult( checkedInFiles );
    }
}
