package org.apache.maven.scm.provider.git.jgit.command.changelog;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.GitChangeSet;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.eclipse.jgit.simple.ChangeEntry;
import org.eclipse.jgit.simple.SimpleRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id$
 */
public class JGitChangeLogCommand
    extends AbstractChangeLogCommand
    implements GitCommand
{
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          ScmVersion startVersion, ScmVersion endVersion,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, null, null, null, datePattern, startVersion, endVersion );
    }

    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, startDate, endDate, branch, datePattern, null, null );
    }

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern, ScmVersion startVersion,
                                                          ScmVersion endVersion )
        throws ScmException
    {
        try
        {
            SimpleRepository srep = SimpleRepository.existing( fileSet.getBasedir() );

            List<GitChangeSet> modifications = new ArrayList<GitChangeSet>();
            
            String startRev = startVersion != null ? startVersion.getName() : null;
            String endRev   = endVersion != null ? endVersion.getName() : null;
            
            List<ChangeEntry> gitChanges = srep.whatchanged( null, startRev, endRev, startDate, endDate, -1 );
            
            for ( ChangeEntry change : gitChanges ) {
                GitChangeSet scmChange = new GitChangeSet();
                
                scmChange.setAuthor( change.getAuthorName() );
                scmChange.setComment( change.getBody() );
                scmChange.setDate( change.getAuthorDate() );
                //X TODO scmChange.setFiles( change.get )
                
                modifications.add( scmChange );
            }
            
            ChangeLogSet changeLogSet = new ChangeLogSet( modifications, startDate, endDate );
            changeLogSet.setStartVersion( startVersion );
            changeLogSet.setEndVersion( endVersion );
            
            return new ChangeLogScmResult( "JGit changelog", changeLogSet );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit changelog failure!", e );
        }
    }
}
