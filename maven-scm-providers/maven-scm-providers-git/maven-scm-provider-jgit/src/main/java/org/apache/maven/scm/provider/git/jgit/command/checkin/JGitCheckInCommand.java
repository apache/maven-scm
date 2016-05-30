package org.apache.maven.scm.provider.git.jgit.command.checkin;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.UserConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This provider uses the following strategy to discover the committer and author name/mail for a commit:
 * <ol>
 * <li>"user" section in .gitconfig</li>
 * <li>"username" passed to maven execution</li>
 * <li>default git config (system user and hostname for email)</li>
 * </ol>
 * the "maven-scm" config can be configured like this: <br>
 * the default email domain to be used (will be used to create an email from the username passed to maven):<br>
 * <code>git config --global maven-scm.maildomain mycomp.com</code> <br>
 * you can also enforce the usage of the username for the author and committer:<br>
 * <code>git config --global maven-scm.forceUsername true</code> <br>
 * 
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
public class JGitCheckInCommand
    extends AbstractCheckInCommand
    implements GitCommand
{

    protected static final String GIT_MAVEN_SECTION = "maven-scm";

    protected static final String GIT_MAILDOMAIN = "maildomain";

    protected static final String GIT_FORCE = "forceUsername";

    /**
     * {@inheritDoc}
     */
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      ScmVersion version )
        throws ScmException
    {

        Git git = null;
        try
        {
            File basedir = fileSet.getBasedir();
            git = JGitUtils.openRepo( basedir );

            boolean doCommit = false;

            if ( !fileSet.getFileList().isEmpty() )
            {
                doCommit = JGitUtils.addAllFiles( git, fileSet ).size() > 0;
            }
            else
            {
                // add all tracked files which are modified manually
                Set<String> changeds = git.status().call().getModified();
                if ( changeds.isEmpty() )
                {
                    // warn there is nothing to add
                    getLogger().warn( "there are no files to be added" );
                    doCommit = false;
                }
                else
                {
                    AddCommand add = git.add();
                    for ( String changed : changeds )
                    {
                        getLogger().debug( "add manualy: " + changed );
                        add.addFilepattern( changed );
                        doCommit = true;
                    }
                    add.call();
                }
            }

            List<ScmFile> checkedInFiles = Collections.emptyList();
            if ( doCommit )
            {
                UserInfo author = getAuthor( repo, git );
                UserInfo committer = getCommitter( repo, git );

                CommitCommand command = git.commit().setMessage( message ).setAuthor( author.name, author.email );
                command.setCommitter( committer.name, committer.email );
                RevCommit commitRev = command.call();

                getLogger().info( "commit done: " + commitRev.getShortMessage() );
                checkedInFiles = JGitUtils.getFilesInCommit( git.getRepository(), commitRev );
                if ( getLogger().isDebugEnabled() )
                {
                    for ( ScmFile scmFile : checkedInFiles )
                    {
                        getLogger().debug( "in commit: " + scmFile );
                    }
                }
            }

            if ( repo.isPushChanges() )
            {
                String branch = version != null ? version.getName() : null;
                if ( StringUtils.isBlank( branch ) )
                {
                    branch = git.getRepository().getBranch();
                }
                RefSpec refSpec = new RefSpec( Constants.R_HEADS + branch + ":" + Constants.R_HEADS + branch );
                getLogger().info( "push changes to remote... " + refSpec.toString() );
                JGitUtils.push( getLogger(), git, (GitScmProviderRepository) repo, refSpec );
            }

            return new CheckInScmResult( "JGit checkin", checkedInFiles );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit checkin failure!", e );
        }
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }

    private static final class UserInfo
    {
        final String name;

        final String email;

        public UserInfo( String name, String email )
        {
            this.name = name;
            this.email = email;
        }
    }

    private UserInfo getCommitter( ScmProviderRepository repo, Git git )
    {
        boolean forceMvnUser = git.getRepository().getConfig().getBoolean( GIT_MAVEN_SECTION, GIT_FORCE, false );

        // git config
        UserConfig user = git.getRepository().getConfig().get( UserConfig.KEY );
        String committerName = null;
        if ( !forceMvnUser && !user.isCommitterNameImplicit() )
        {
            committerName = user.getCommitterName();
        }

        // mvn parameter
        if ( StringUtils.isBlank( committerName ) )
        {
            committerName = repo.getUser();
        }

        // git default
        if ( StringUtils.isBlank( committerName ) )
        {
            committerName = user.getCommitterName();
        }

        // git config
        String committerMail = null;
        if ( !user.isCommitterEmailImplicit() )
        {
            committerMail = user.getCommitterEmail();
        }

        if ( StringUtils.isBlank( committerMail ) )
        {
            String defaultDomain = git.getRepository().getConfig().getString( GIT_MAVEN_SECTION, null, GIT_MAILDOMAIN );
            defaultDomain = StringUtils.isNotBlank( defaultDomain ) ? defaultDomain : getHostname();

            // mvn parameter (constructed with username) or git default
            committerMail =
                StringUtils.isNotBlank( repo.getUser() ) ? repo.getUser() + "@" + defaultDomain
                                : user.getCommitterEmail();
        }

        return new UserInfo( committerName, committerMail );
    }

    private UserInfo getAuthor( ScmProviderRepository repo, Git git )
    {
        boolean forceMvnUser = git.getRepository().getConfig().getBoolean( GIT_MAVEN_SECTION, GIT_FORCE, false );

        // git config
        UserConfig user = git.getRepository().getConfig().get( UserConfig.KEY );
        String authorName = null;
        if ( !forceMvnUser && !user.isAuthorNameImplicit() )
        {
            authorName = user.getAuthorName();
        }

        // mvn parameter
        if ( StringUtils.isBlank( authorName ) )
        {
            authorName = repo.getUser();
        }

        // git default
        if ( StringUtils.isBlank( authorName ) )
        {
            authorName = user.getAuthorName();
        }

        // git config
        String authorMail = null;
        if ( !user.isAuthorEmailImplicit() )
        {
            authorMail = user.getAuthorEmail();
        }

        if ( StringUtils.isBlank( authorMail ) )
        {
            String defaultDomain = git.getRepository().getConfig().getString( GIT_MAVEN_SECTION, null, GIT_MAILDOMAIN );
            defaultDomain = StringUtils.isNotBlank( defaultDomain ) ? defaultDomain : getHostname();

            // mvn parameter (constructed with username) or git default
            authorMail =
                StringUtils.isNotBlank( repo.getUser() ) ? repo.getUser() + "@" + defaultDomain : user.getAuthorEmail();
        }

        return new UserInfo( authorName, authorMail );
    }

    private String getHostname()
    {
        String hostname;
        try
        {
            InetAddress localhost = java.net.InetAddress.getLocalHost();
            hostname = localhost.getHostName();
        }
        catch ( UnknownHostException e )
        {
            getLogger().warn( "failed to resolve hostname to create mail address, "
                                  + "defaulting to 'maven-scm-provider-jgit'" );
            hostname = "maven-scm-provider-jgit";
        }
        return hostname;
    }

}
