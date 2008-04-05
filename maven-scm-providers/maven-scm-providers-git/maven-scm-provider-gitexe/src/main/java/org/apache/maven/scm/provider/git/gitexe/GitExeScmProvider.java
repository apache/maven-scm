package org.apache.maven.scm.provider.git.gitexe;

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
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.provider.git.AbstractGitScmProvider;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.command.info.GitInfoItem;
import org.apache.maven.scm.provider.git.command.info.GitInfoScmResult;
import org.apache.maven.scm.provider.git.gitexe.command.add.GitAddCommand;
import org.apache.maven.scm.provider.git.gitexe.command.branch.GitBranchCommand;
import org.apache.maven.scm.provider.git.gitexe.command.changelog.GitChangeLogCommand;
import org.apache.maven.scm.provider.git.gitexe.command.checkin.GitCheckInCommand;
import org.apache.maven.scm.provider.git.gitexe.command.checkout.GitCheckOutCommand;
import org.apache.maven.scm.provider.git.gitexe.command.diff.GitDiffCommand;
import org.apache.maven.scm.provider.git.gitexe.command.remove.GitRemoveCommand;
import org.apache.maven.scm.provider.git.gitexe.command.status.GitStatusCommand;
import org.apache.maven.scm.provider.git.gitexe.command.tag.GitTagCommand;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="git"
 */
public class GitExeScmProvider
    extends AbstractGitScmProvider
{
    protected GitCommand getAddCommand()
    {
        return new GitAddCommand();
    }

    protected GitCommand getBranchCommand()
    {
        return new GitBranchCommand();
    }

    protected GitCommand getChangeLogCommand()
    {
        return new GitChangeLogCommand();
    }

    protected GitCommand getCheckInCommand()
    {
        return new GitCheckInCommand();
    }

    protected GitCommand getCheckOutCommand()
    {
        return new GitCheckOutCommand();
    }

    protected GitCommand getDiffCommand()
    {
        return new GitDiffCommand();
    }

    protected GitCommand getExportCommand()
    {
        return null; //X TODO
    }

    protected GitCommand getRemoveCommand()
    {
        return new GitRemoveCommand();
    }

    protected GitCommand getStatusCommand()
    {
        return new GitStatusCommand();
    }

    protected GitCommand getTagCommand()
    {
        return new GitTagCommand();
    }

    protected GitCommand getUpdateCommand()
    {
        return null; //X TODO
    }

    protected GitCommand getListCommand()
    {
        return null; //X TODO
    }

    public GitCommand getInfoCommand()
    {
        return null; //X TODO
    }

    /**
     * Implements retrieving the repository url for a certain path using the 'git info' command.
     */
    protected String getRepositoryURL( File path )
        throws ScmException
    {
        // Note: I need to supply just 1 absolute path, but ScmFileSet won't let me without
        // a basedir (which isn't used here anyway), so use a dummy file.
        GitInfoScmResult result = info( null, new ScmFileSet( new File( "" ), path ), null );

        if ( result.getInfoItems().size() != 1 )
        {
            throw new ScmRepositoryException( "Cannot find URL: " +
                ( result.getInfoItems().size() == 0 ? "no" : "multiple" ) + " items returned by the info command" );
        }

        return ( (GitInfoItem) result.getInfoItems().get( 0 ) ).getURL();
    }
}
