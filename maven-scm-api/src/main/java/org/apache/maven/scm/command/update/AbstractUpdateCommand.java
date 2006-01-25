package org.apache.maven.scm.command.update;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractUpdateCommand
    extends AbstractCommand
{
    protected abstract UpdateScmResult executeUpdateCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                             String tag )
        throws ScmException;

    public ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        String tag = parameters.getString( CommandParameter.TAG, null );

        UpdateScmResult updateScmResult = executeUpdateCommand( repository, fileSet, tag );

        List filesList = updateScmResult.getUpdatedFiles();

        ChangeLogCommand changeLogCmd = getChangeLogCommand();

        if ( filesList != null && filesList.size() > 0 && changeLogCmd != null )
        {
            ChangeLogScmResult changeLogScmResult =
                (ChangeLogScmResult) changeLogCmd.executeCommand( repository, fileSet, parameters );

            List changes = new ArrayList();

            List changesList = changeLogScmResult.getChangeLog();

            if ( changesList != null )
            {
                for ( Iterator i = changesList.iterator(); i.hasNext(); )
                {
                    ChangeSet change = (ChangeSet) i.next();

                    for ( Iterator j = filesList.iterator(); j.hasNext(); )
                    {
                        ScmFile currentFile = (ScmFile) j.next();

                        if ( change.containsFilename( currentFile.getPath(), repository ) )
                        {
                            changes.add( change );
                        }
                    }
                }
            }

            updateScmResult.setChanges( changes );
        }
        else
        {
            updateScmResult.setChanges( new ArrayList() );
        }

        return updateScmResult;
    }

    protected abstract ChangeLogCommand getChangeLogCommand();
}
