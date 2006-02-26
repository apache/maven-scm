package org.apache.maven.scm.provider.svn.svnexe;

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

import org.apache.maven.scm.provider.svn.AbstractSvnScmProvider;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.add.SvnAddCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.changelog.SvnChangeLogCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.checkin.SvnCheckInCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.checkout.SvnCheckOutCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.diff.SvnDiffCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.remove.SvnRemoveCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.status.SvnStatusCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.tag.SvnTagCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.update.SvnUpdateCommand;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
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
}
