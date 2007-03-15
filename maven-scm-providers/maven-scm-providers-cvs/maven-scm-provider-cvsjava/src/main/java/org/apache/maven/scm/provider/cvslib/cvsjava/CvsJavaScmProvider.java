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
import org.apache.maven.scm.provider.cvslib.command.login.CvsLoginCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.changelog.CvsJavaChangeLogCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.add.CvsJavaAddCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.checkin.CvsJavaCheckInCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.checkout.CvsJavaCheckOutCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.diff.CvsJavaDiffCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.list.CvsJavaListCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.remove.CvsJavaRemoveCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.status.CvsJavaStatusCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.tag.CvsJavaTagCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.update.CvsJavaUpdateCommand;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsJavaScmProvider
    extends AbstractCvsScmProvider
{
    protected Command getAddCommand()
    {
        return new CvsJavaAddCommand();
    }

    protected Command getChangeLogCommand()
    {
        return new CvsJavaChangeLogCommand();
    }

    protected Command getCheckInCommand()
    {
        return new CvsJavaCheckInCommand();
    }

    protected Command getCheckOutCommand()
    {
        return new CvsJavaCheckOutCommand();
    }

    protected Command getDiffCommand()
    {
        return new CvsJavaDiffCommand();
    }

    protected Command getListCommand()
    {
        return new CvsJavaListCommand();
    }

    protected Command getLoginCommand()
    {
        return new CvsLoginCommand();
    }

    protected Command getRemoveCommand()
    {
        return new CvsJavaRemoveCommand();
    }

    protected Command getStatusCommand()
    {
        return new CvsJavaStatusCommand();
    }

    protected Command getTagCommand()
    {
        return new CvsJavaTagCommand();
    }

    protected Command getUpdateCommand()
    {
        return new CvsJavaUpdateCommand();
    }
}
