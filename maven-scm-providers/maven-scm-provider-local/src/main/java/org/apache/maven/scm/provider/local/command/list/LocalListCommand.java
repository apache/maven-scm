package org.apache.maven.scm.provider.local.command.list;

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
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class LocalListCommand
    extends AbstractListCommand
{
    /**
     * List contents of the remote repository
     *
     * @param repo      what to list
     * @param fileSet   the files to list
     * @param recursive whether list should return subfolder listing
     * @return the list of files
     * @throws org.apache.maven.scm.ScmException
     *
     */
    protected ListScmResult executeListCommand( ScmProviderRepository repo, ScmFileSet fileSet, boolean recursive,
                                                String tag )
        throws ScmException
    {
        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "The local scm doesn't support tags." );
        }

        LocalScmProviderRepository repository = (LocalScmProviderRepository) repo;

        File root = new File( repository.getRoot() );

        String module = repository.getModule();

        File source = new File( root, module );

        if ( !root.exists() )
        {
            throw new ScmException( "The base directory doesn't exist (" + root.getAbsolutePath() + ")." );
        }

        if ( !source.exists() )
        {
            throw new ScmException( "The module directory doesn't exist (" + source.getAbsolutePath() + ")." );
        }

        getLogger().info( "Listing files of '" + source.getAbsolutePath() + "'." );

        try
        {
            if ( fileSet.getFileList() == null || fileSet.getFileList().isEmpty() )
            {
                return new LocalListScmResult( null, getFiles( source, source, recursive ) );
            }
            else
            {
                List files = new ArrayList();
                Iterator it = fileSet.getFileList().iterator();

                while ( it.hasNext() )
                {
                    File file = (File) it.next();

                    files.addAll( getFiles( source, new File( source, file.getPath() ), recursive ) );
                }

                return new LocalListScmResult( null, files );
            }
        }
        catch ( Exception e )
        {
            return new ListScmResult( null, "The svn command failed.", e.getMessage(), false );
        }
    }

    private List getFiles( File source, File directory, boolean recursive )
        throws Exception
    {
        if ( !directory.exists() )
        {
            throw new Exception( "Directory '" + directory.getAbsolutePath() + "' doesn't exist." );
        }

        List files = new ArrayList();

        File[] filesArray = directory.listFiles();

        if ( filesArray != null )
        {
            for ( int i = 0; i < filesArray.length; i++ )
            {
                File f = filesArray[i];

                String path = f.getAbsolutePath().substring( source.getAbsolutePath().length() );
                path = StringUtils.replace( path, "\\", "/" );
                path = StringUtils.replace( path, "/./", "/" );

                files.add( new ScmFile( path, ScmFileStatus.CHECKED_IN ) );

                if ( f.isDirectory() && recursive )
                {
                    files.addAll( getFiles( source, f, recursive ) );
                }
            }
        }

        return files;
    }
}
