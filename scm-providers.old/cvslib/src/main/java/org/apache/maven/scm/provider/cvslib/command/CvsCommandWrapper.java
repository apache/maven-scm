package org.apache.maven.scm.provider.cvslib.command;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * ====================================================================
 */

import java.util.Enumeration;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.AbstractCommandWrapper;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.provider.cvslib.command.changelog.CvsChangeLogCommand;
import org.apache.maven.scm.provider.cvslib.command.checkout.CvsCheckOutCommand;
import org.apache.maven.scm.provider.cvslib.command.update.CvsUpdateCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsRepository;
import org.apache.maven.scm.repository.Repository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsCommandWrapper extends AbstractCommandWrapper
{
    private CvsRepository repository;

    public CvsCommandWrapper() throws ScmException
    {
        CvsChangeLogCommand changelog = new CvsChangeLogCommand();
        getCommandsTable().put(ChangeLogCommand.NAME, changelog);
		CvsCheckOutCommand co = new CvsCheckOutCommand();
		getCommandsTable().put(CvsCheckOutCommand.NAME, co);
		CvsUpdateCommand update = new CvsUpdateCommand();
		getCommandsTable().put(CvsUpdateCommand.NAME, update);
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.CommandWrapper#setRepository(org.apache.maven.scm.repository.Repository)
     */
    public void setRepository(Repository repository) throws ScmException
    {
        if (repository instanceof CvsRepository)
        {
            this.repository = (CvsRepository)repository;

            for (Enumeration e = getCommandsTable().elements(); e.hasMoreElements(); )
            {
                Command command = (Command)e.nextElement();
                command.setRepository(repository);
            }
        }
        else
        {
            throw new ScmException("repository must be an instance of CvsRepository");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.CommandWrapper#getRepository()
     */
    public Repository getRepository()
    {
        return repository;
    }
}
