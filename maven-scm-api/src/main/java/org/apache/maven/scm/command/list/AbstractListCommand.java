package org.apache.maven.scm.command.list;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public abstract class AbstractListCommand
    extends AbstractCommand
{
    /**
     * List contents of the remote repository
     * 
     * @param repository what to list
     * @param fileSet the files to list
     * @param recursive whether list should return subfolder listing
     * @return the list of files
     * @throws ScmException
     */
    protected abstract ListScmResult executeListCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                         boolean recursive )
        throws ScmException;

    public ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        if ( fileSet.getFileList().isEmpty() )
        {
            throw new IllegalArgumentException( "fileSet can not be empty" );
        }

        boolean recursive = parameters.getBoolean( CommandParameter.RECURSIVE );

        return executeListCommand( repository, fileSet, recursive );
    }
}