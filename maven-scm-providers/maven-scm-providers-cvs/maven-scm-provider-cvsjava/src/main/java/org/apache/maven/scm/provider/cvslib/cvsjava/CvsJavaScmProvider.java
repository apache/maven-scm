package org.apache.maven.scm.provider.cvslib.cvsjava;

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
import org.apache.maven.scm.provider.cvslib.cvsjava.command.add.CvsJavaAddCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.branch.CvsJavaBranchCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.changelog.CvsJavaChangeLogCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.checkin.CvsJavaCheckInCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.checkout.CvsJavaCheckOutCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.diff.CvsJavaDiffCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.export.CvsJavaExportCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.list.CvsJavaListCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.login.CvsJavaLoginCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.remove.CvsJavaRemoveCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.status.CvsJavaStatusCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.tag.CvsJavaTagCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.update.CvsJavaUpdateCommand;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="cvs"
 */
public class CvsJavaScmProvider
    extends AbstractCvsScmProvider
{
    /** {@inheritDoc} */
    protected Command getAddCommand()
    {
        return new CvsJavaAddCommand();
    }

    /** {@inheritDoc} */
    protected Command getBranchCommand()
    {
        return new CvsJavaBranchCommand();
    }

    /** {@inheritDoc} */
    protected Command getChangeLogCommand()
    {
        return new CvsJavaChangeLogCommand();
    }

    /** {@inheritDoc} */
    protected Command getCheckInCommand()
    {
        return new CvsJavaCheckInCommand();
    }

    /** {@inheritDoc} */
    protected Command getCheckOutCommand()
    {
        return new CvsJavaCheckOutCommand();
    }

    /** {@inheritDoc} */
    protected Command getDiffCommand()
    {
        return new CvsJavaDiffCommand();
    }

    /** {@inheritDoc} */
    protected Command getExportCommand()
    {
        return new CvsJavaExportCommand();
    }

    /** {@inheritDoc} */
    protected Command getListCommand()
    {
        return new CvsJavaListCommand();
    }

    /** {@inheritDoc} */
    protected Command getLoginCommand()
    {
        return new CvsJavaLoginCommand();
    }

    /** {@inheritDoc} */
    protected Command getRemoveCommand()
    {
        return new CvsJavaRemoveCommand();
    }

    /** {@inheritDoc} */
    protected Command getStatusCommand()
    {
        return new CvsJavaStatusCommand();
    }

    /** {@inheritDoc} */
    protected Command getTagCommand()
    {
        return new CvsJavaTagCommand();
    }

    /** {@inheritDoc} */
    protected Command getUpdateCommand()
    {
        return new CvsJavaUpdateCommand();
    }
}
