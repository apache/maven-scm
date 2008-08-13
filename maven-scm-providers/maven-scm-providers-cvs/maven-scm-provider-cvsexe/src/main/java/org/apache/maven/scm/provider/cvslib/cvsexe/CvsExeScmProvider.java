package org.apache.maven.scm.provider.cvslib.cvsexe;

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

import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmProvider;
import org.apache.maven.scm.provider.cvslib.command.login.CvsLoginCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.add.CvsExeAddCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.branch.CvsExeBranchCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.changelog.CvsExeChangeLogCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.checkin.CvsExeCheckInCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.checkout.CvsExeCheckOutCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.diff.CvsExeDiffCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.export.CvsExeExportCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.list.CvsExeListCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.remove.CvsExeRemoveCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.status.CvsExeStatusCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.tag.CvsExeTagCommand;
import org.apache.maven.scm.provider.cvslib.cvsexe.command.update.CvsExeUpdateCommand;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="cvs_native"
 */
public class CvsExeScmProvider
    extends AbstractCvsScmProvider
{
    /** {@inheritDoc} */
    protected Command getAddCommand()
    {
        return new CvsExeAddCommand();
    }

    /** {@inheritDoc} */
    protected Command getBranchCommand()
    {
        return new CvsExeBranchCommand();
    }

    /** {@inheritDoc} */
    protected Command getChangeLogCommand()
    {
        return new CvsExeChangeLogCommand();
    }

    /** {@inheritDoc} */
    protected Command getCheckInCommand()
    {
        return new CvsExeCheckInCommand();
    }

    /** {@inheritDoc} */
    protected Command getCheckOutCommand()
    {
        return new CvsExeCheckOutCommand();
    }

    /** {@inheritDoc} */
    protected Command getDiffCommand()
    {
        return new CvsExeDiffCommand();
    }

    /** {@inheritDoc} */
    protected Command getExportCommand()
    {
        return new CvsExeExportCommand();
    }

    /** {@inheritDoc} */
    protected Command getListCommand()
    {
        return new CvsExeListCommand();
    }

    /** {@inheritDoc} */
    protected Command getLoginCommand()
    {
        return new CvsLoginCommand();
    }

    /** {@inheritDoc} */
    protected Command getRemoveCommand()
    {
        return new CvsExeRemoveCommand();
    }

    /** {@inheritDoc} */
    protected Command getStatusCommand()
    {
        return new CvsExeStatusCommand();
    }

    /** {@inheritDoc} */
    protected Command getTagCommand()
    {
        return new CvsExeTagCommand();
    }

    /** {@inheritDoc} */
    protected Command getUpdateCommand()
    {
        return new CvsExeUpdateCommand();
    }
}
