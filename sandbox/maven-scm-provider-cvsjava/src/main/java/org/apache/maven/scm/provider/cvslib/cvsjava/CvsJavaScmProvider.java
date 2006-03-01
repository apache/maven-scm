package org.apache.maven.scm.provider.cvslib.cvsjava;

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
import org.apache.maven.scm.provider.cvslib.command.login.CvsLoginCommand;
import org.apache.maven.scm.provider.cvslib.cvsjava.command.changelog.CvsJavaChangeLogCommand;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsJavaScmProvider
    extends AbstractCvsScmProvider
{
    protected Command getAddCommand()
    {
        return null;//return new CvsJavaAddCommand();
    }

    protected Command getChangeLogCommand()
    {
        return new CvsJavaChangeLogCommand();
    }

    protected Command getCheckInCommand()
    {
        return null;//return new CvsJavaCheckInCommand();
    }

    protected Command getCheckOutCommand()
    {
        return null;//return new CvsJavaCheckOutCommand();
    }

    protected Command getDiffCommand()
    {
        return null;//return new CvsJavaDiffCommand();
    }

    protected Command getLoginCommand()
    {
        return new CvsLoginCommand();
    }

    protected Command getRemoveCommand()
    {
        return null;//return new CvsJavaRemoveCommand();
    }

    protected Command getStatusCommand()
    {
        return null;//return new CvsJavaStatusCommand();
    }

    protected Command getTagCommand()
    {
        return null;//return new CvsJavaTagCommand();
    }

    protected Command getUpdateCommand()
    {
        return null;//return new CvsJavaUpdateCommand();
    }
}
