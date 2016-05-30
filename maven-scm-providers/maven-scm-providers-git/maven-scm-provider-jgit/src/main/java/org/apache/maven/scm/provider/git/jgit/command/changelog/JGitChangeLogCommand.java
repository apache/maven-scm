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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
public class JGitChangeLogCommand
    extends AbstractChangeLogCommand
    implements GitCommand
{

    /**
     * {@inheritDoc}
     */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          ScmVersion startVersion, ScmVersion endVersion,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, null, null, null, datePattern, startVersion, endVersion );
    }

    /**
     * {@inheritDoc}
     */
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
        Git git = null;
        try
        {
            git = JGitUtils.openRepo( fileSet.getBasedir() );

            String startRev = startVersion != null ? startVersion.getName() : null;
            String endRev = endVersion != null ? endVersion.getName() : null;

            List<ChangeEntry> gitChanges =
                this.whatchanged( git.getRepository(), null, startRev, endRev, startDate, endDate, -1 );

            List<ChangeSet> modifications = new ArrayList<ChangeSet>( gitChanges.size() );

            for ( ChangeEntry change : gitChanges )
            {
                ChangeSet scmChange = new ChangeSet();

                scmChange.setAuthor( change.getAuthorName() );
                scmChange.setComment( change.getBody() );
                scmChange.setDate( change.getAuthorDate() );
                scmChange.setRevision( change.getCommitHash() );
                // X TODO scmChange.setFiles( change.get )

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
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }

    public List<ChangeEntry> whatchanged( Repository repo, RevSort[] sortings, String fromRev, String toRev,
                                          Date fromDate, Date toDate, int maxLines )
        throws MissingObjectException, IncorrectObjectTypeException, IOException
    {
        List<RevCommit> revs = JGitUtils.getRevCommits( repo, sortings, fromRev, toRev, fromDate, toDate, maxLines );
        List<ChangeEntry> changes = new ArrayList<ChangeEntry>( revs.size() );

        for ( RevCommit c : revs )
        {
            ChangeEntry ce = new ChangeEntry();

            ce.setAuthorDate( c.getAuthorIdent().getWhen() );
            ce.setAuthorEmail( c.getAuthorIdent().getEmailAddress() );
            ce.setAuthorName( c.getAuthorIdent().getName() );
            ce.setCommitterDate( c.getCommitterIdent().getWhen() );
            ce.setCommitterEmail( c.getCommitterIdent().getEmailAddress() );
            ce.setCommitterName( c.getCommitterIdent().getName() );

            ce.setSubject( c.getShortMessage() );
            ce.setBody( c.getFullMessage() );

            ce.setCommitHash( c.getId().name() );
            ce.setTreeHash( c.getTree().getId().name() );

            // X TODO missing: file list

            changes.add( ce );
        }

        return changes;
    }

    /**
     * 
     */
    public static final class ChangeEntry
    {
        private String commitHash;

        private String treeHash;

        private String authorName;

        private String authorEmail;

        private Date authorDate;

        private String committerName;

        private String committerEmail;

        private Date committerDate;

        private String subject;

        private String body;

        private List<File> files;

        public String getCommitHash()
        {
            return commitHash;
        }

        public void setCommitHash( String commitHash )
        {
            this.commitHash = commitHash;
        }

        public String getTreeHash()
        {
            return treeHash;
        }

        public void setTreeHash( String treeHash )
        {
            this.treeHash = treeHash;
        }

        public String getAuthorName()
        {
            return authorName;
        }

        public void setAuthorName( String authorName )
        {
            this.authorName = authorName;
        }

        public String getAuthorEmail()
        {
            return authorEmail;
        }

        public void setAuthorEmail( String authorEmail )
        {
            this.authorEmail = authorEmail;
        }

        public Date getAuthorDate()
        {
            return authorDate;
        }

        public void setAuthorDate( Date authorDate )
        {
            this.authorDate = authorDate;
        }

        public String getCommitterName()
        {
            return committerName;
        }

        public void setCommitterName( String committerName )
        {
            this.committerName = committerName;
        }

        public String getCommitterEmail()
        {
            return committerEmail;
        }

        public void setCommitterEmail( String committerEmail )
        {
            this.committerEmail = committerEmail;
        }

        public Date getCommitterDate()
        {
            return committerDate;
        }

        public void setCommitterDate( Date committerDate )
        {
            this.committerDate = committerDate;
        }

        public String getSubject()
        {
            return subject;
        }

        public void setSubject( String subject )
        {
            this.subject = subject;
        }

        public String getBody()
        {
            return body;
        }

        public void setBody( String body )
        {
            this.body = body;
        }

        public List<File> getFiles()
        {
            return files;
        }

        public void setFiles( List<File> files )
        {
            this.files = files;
        }
    }
}
