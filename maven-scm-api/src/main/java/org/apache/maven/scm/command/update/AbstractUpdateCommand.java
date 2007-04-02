package org.apache.maven.scm.command.update;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;

import java.util.ArrayList;
import java.util.Date;
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
                                                             ScmVersion scmVersion )
        throws ScmException;

    public ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        ScmVersion scmVersion = parameters.getScmVersion( CommandParameter.SCM_VERSION, null );

        boolean runChangelog = Boolean.valueOf(
            parameters.getString( CommandParameter.RUN_CHANGELOG_WITH_UPDATE, "true" ) ).booleanValue();

        UpdateScmResult updateScmResult = executeUpdateCommand( repository, fileSet, scmVersion );

        List filesList = updateScmResult.getUpdatedFiles();

        if ( !runChangelog )
        {
            return updateScmResult;
        }

        ChangeLogCommand changeLogCmd = getChangeLogCommand();

        if ( filesList != null && filesList.size() > 0 && changeLogCmd != null )
        {
            ChangeLogScmResult changeLogScmResult =
                (ChangeLogScmResult) changeLogCmd.executeCommand( repository, fileSet, parameters );

            List changes = new ArrayList();

            ChangeLogSet changeLogSet = changeLogScmResult.getChangeLog();

            if ( changeLogSet != null )
            {
                Date startDate = null;

                try
                {
                    startDate = parameters.getDate( CommandParameter.START_DATE );
                }
                catch ( ScmException e )
                {
                    //Do nothing, startDate isn't define.
                }

                for ( Iterator i = changeLogSet.getChangeSets().iterator(); i.hasNext(); )
                {
                    ChangeSet change = (ChangeSet) i.next();

                    if ( startDate != null && change.getDate() != null )
                    {
                        if ( startDate.after( change.getDate() ) )
                        {
                            continue;
                        }
                    }

                    for ( Iterator j = filesList.iterator(); j.hasNext(); )
                    {
                        ScmFile currentFile = (ScmFile) j.next();

                        if ( change.containsFilename( currentFile.getPath(), repository ) )
                        {
                            changes.add( change );

                            break;
                        }
                    }
                }
            }

            updateScmResult.setChanges( changes );
        }

        return updateScmResult;
    }

    protected abstract ChangeLogCommand getChangeLogCommand();
}
