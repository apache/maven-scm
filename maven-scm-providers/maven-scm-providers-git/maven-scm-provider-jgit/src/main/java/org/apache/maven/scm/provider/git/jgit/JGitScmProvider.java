package org.apache.maven.scm.provider.git.jgit;

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
import org.apache.maven.scm.provider.git.jgit.command.add.JGitAddCommand;
import org.apache.maven.scm.provider.git.jgit.command.changelog.JGitChangeLogCommand;
import org.apache.maven.scm.provider.git.jgit.command.checkin.JGitCheckInCommand;
import org.apache.maven.scm.provider.git.jgit.command.checkout.JGitCheckOutCommand;
import org.apache.maven.scm.provider.git.jgit.command.status.JGitStatusCommand;
import org.apache.maven.scm.provider.git.jgit.command.tag.JGitTagCommand;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="jgit"
 */
public class JGitScmProvider
    extends AbstractGitScmProvider
{
    /** {@inheritDoc} */
    protected GitCommand getAddCommand()
    {
        return new JGitAddCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getBranchCommand()
    {
        return null;
    }

    /** {@inheritDoc} */
    protected GitCommand getChangeLogCommand()
    {
        return new JGitChangeLogCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getCheckInCommand()
    {
        return new JGitCheckInCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getCheckOutCommand()
    {
        return new JGitCheckOutCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getDiffCommand()
    {
        return null;
    }

    /** {@inheritDoc} */
    protected GitCommand getExportCommand()
    {
        return null; //X TODO
    }

    /** {@inheritDoc} */
    protected GitCommand getRemoveCommand()
    {
        return null;
    }

    /** {@inheritDoc} */
    protected GitCommand getStatusCommand()
    {
        return new JGitStatusCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getTagCommand()
    {
        return new JGitTagCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getUpdateCommand()
    {
        return null;
    }

    /** {@inheritDoc} */
    protected GitCommand getListCommand()
    {
        return null;
    }

    /** {@inheritDoc} */
    public GitCommand getInfoCommand()
    {
        return null; //X TODO
    }

    /** {@inheritDoc} */
    protected String getRepositoryURL( File path )
        throws ScmException
    {
        // Note: I need to supply just 1 absolute path, but ScmFileSet won't let me without
        // a basedir (which isn't used here anyway), so use a dummy file.
        GitInfoScmResult result = info( null, new ScmFileSet( new File( "" ), path ), null );

        if ( result.getInfoItems().size() != 1 )
        {
            throw new ScmRepositoryException( "Cannot find URL: "
                + ( result.getInfoItems().size() == 0 ? "no" : "multiple" ) + " items returned by the info command" );
        }

        return ( (GitInfoItem) result.getInfoItems().get( 0 ) ).getURL();
    }
}
