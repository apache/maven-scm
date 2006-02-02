package org.apache.maven.scm.provider.local.command.changelog;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class LocalChangeLogCommand
    extends AbstractChangeLogCommand
{
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, String branch,
                                                          String datePattern )
        throws ScmException
    {
        LocalScmProviderRepository repo = (LocalScmProviderRepository) repository;

        if ( !StringUtils.isEmpty( branch ) )
        {
            throw new ScmException( "The local scm doesn't support tags." );
        }

        File root = new File( repo.getRoot() );

        String module = repo.getModule();

        File source = new File( root, module );

        File baseDestination = fileSet.getBasedir();

        if ( !baseDestination.exists() )
        {
            throw new ScmException(
                "The working directory doesn't exist (" + baseDestination.getAbsolutePath() + ")." );
        }

        if ( !root.exists() )
        {
            throw new ScmException( "The base directory doesn't exist (" + root.getAbsolutePath() + ")." );
        }

        if ( !source.exists() )
        {
            throw new ScmException( "The module directory doesn't exist (" + source.getAbsolutePath() + ")." );
        }

        List changeLogList = new ArrayList();

        try
        {
            File repoRoot = new File( repo.getRoot(), repo.getModule() );

            List files = Arrays.asList( fileSet.getFiles() );

            if ( files.isEmpty() )
            {
                files = FileUtils.getFiles( baseDestination, "**", null, false );
            }

            Iterator it = files.iterator();

            while ( it.hasNext() )
            {
                File file = (File) it.next();

                String path = file.getPath().replace( '\\', '/' );

                File repoFile = new File( repoRoot, path );

                file = new File( baseDestination, path );

                ChangeSet changeSet = new ChangeSet();

                int chop = repoRoot.getAbsolutePath().length();

                String fileName = "/" + repoFile.getAbsolutePath().substring( chop + 1 );

                changeSet.addFile( new ChangeFile( fileName, null ) );

                if ( repoFile.exists() )
                {
                    long lastModified = repoFile.lastModified();

                    Date modifiedDate = new Date( lastModified );

                    if ( startDate != null )
                    {
                        if ( startDate.before( modifiedDate ) || startDate.equals( modifiedDate ) )
                        {
                            if ( endDate != null )
                            {
                                if ( endDate.after( modifiedDate ) || endDate.equals( modifiedDate ) )
                                {
                                }
                                else
                                {
                                    continue;
                                }
                            }
                        }
                        else
                        {
                            continue;
                        }
                    }

                    changeSet.setDate( modifiedDate );

                    changeLogList.add( changeSet );
                }
                else
                {
                    // This file is deleted
                    changeLogList.add( changeSet );
                }
            }
        }
        catch ( IOException ex )
        {
            throw new ScmException( "Error while getting change logs.", ex );
        }

        return new ChangeLogScmResult( null, new ChangeLogSet( changeLogList, startDate, endDate ) );
    }
}
