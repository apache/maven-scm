package org.apache.maven.scm.provider.svn.svnexe;

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
import org.apache.maven.scm.provider.svn.AbstractSvnScmProvider;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.info.SvnInfoItem;
import org.apache.maven.scm.provider.svn.command.info.SvnInfoScmResult;
import org.apache.maven.scm.provider.svn.svnexe.command.add.SvnAddCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.changelog.SvnChangeLogCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.checkin.SvnCheckInCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.checkout.SvnCheckOutCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.diff.SvnDiffCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.export.SvnExeExportCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.info.SvnInfoCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.list.SvnListCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.remove.SvnRemoveCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.status.SvnStatusCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.tag.SvnTagCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.update.SvnUpdateCommand;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="svn"
 */
public class SvnExeScmProvider
    extends AbstractSvnScmProvider
{
    protected SvnCommand getAddCommand()
    {
        return new SvnAddCommand();
    }

    protected SvnCommand getChangeLogCommand()
    {
        return new SvnChangeLogCommand();
    }

    protected SvnCommand getCheckInCommand()
    {
        return new SvnCheckInCommand();
    }

    protected SvnCommand getCheckOutCommand()
    {
        return new SvnCheckOutCommand();
    }

    protected SvnCommand getDiffCommand()
    {
        return new SvnDiffCommand();
    }

    protected SvnCommand getExportCommand()
    {
        return new SvnExeExportCommand();
    }

    protected SvnCommand getRemoveCommand()
    {
        return new SvnRemoveCommand();
    }

    protected SvnCommand getStatusCommand()
    {
        return new SvnStatusCommand();
    }

    protected SvnCommand getTagCommand()
    {
        return new SvnTagCommand();
    }

    protected SvnCommand getUpdateCommand()
    {
        return new SvnUpdateCommand();
    }

    protected SvnCommand getListCommand()
    {
        return new SvnListCommand();
    }

    public SvnCommand getInfoCommand()
    {
        return new SvnInfoCommand();
    }

    /**
     * Implements retrieving the repository url for a certain path using the 'svn info' command.
     */
    protected String getRepositoryURL( File path )
        throws ScmException
    {
        // Note: I need to supply just 1 absolute path, but ScmFileSet won't let me without
        // a basedir (which isn't used here anyway), so use a dummy file.
        SvnInfoScmResult result = info( null, new ScmFileSet( new File( "" ), path ), null );

        if ( result.getInfoItems().size() != 1 )
        {
            throw new ScmRepositoryException( "Cannot find URL: " +
                ( result.getInfoItems().size() == 0 ? "no" : "multiple" ) + " items returned by the info command" );
        }

        return ( (SvnInfoItem) result.getInfoItems().get( 0 ) ).getURL();
    }
}
