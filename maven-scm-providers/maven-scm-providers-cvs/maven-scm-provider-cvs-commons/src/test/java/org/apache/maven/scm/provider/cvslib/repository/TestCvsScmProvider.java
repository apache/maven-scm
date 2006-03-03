package org.apache.maven.scm.provider.cvslib.repository;

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

import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmProvider;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class TestCvsScmProvider
    extends AbstractCvsScmProvider
{
    protected Command getAddCommand()
    {
        return null;
    }

    protected Command getChangeLogCommand()
    {
        return null;
    }

    protected Command getCheckInCommand()
    {
        return null;
    }

    protected Command getCheckOutCommand()
    {
        return null;
    }

    protected Command getDiffCommand()
    {
        return null;
    }

    protected Command getLoginCommand()
    {
        return null;
    }

    protected Command getRemoveCommand()
    {
        return null;
    }

    protected Command getStatusCommand()
    {
        return null;
    }

    protected Command getTagCommand()
    {
        return null;
    }

    protected Command getUpdateCommand()
    {
        return null;
    }
}
