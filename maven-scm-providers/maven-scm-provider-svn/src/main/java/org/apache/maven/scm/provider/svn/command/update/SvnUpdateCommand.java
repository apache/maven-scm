package org.apache.maven.scm.provider.svn.command.update;

/*
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
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.update.UpdateCommand;
import org.apache.maven.scm.provider.svn.command.AbstractSvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnUpdateCommand
    extends AbstractSvnCommand
    implements UpdateCommand
{

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.AbstractCommand#getCommandLine()
     */
    public Commandline getCommandLine() throws ScmException
    {
		Commandline command = new Commandline();

		command.setExecutable("svn");

		if (getWorkingDirectory() != null)
		{
			command.setWorkingDirectory(getWorkingDirectory());
		}

		SvnRepository repo = (SvnRepository)getRepository();

		command.createArgument().setValue("update");
		command.createArgument().setValue("--non-interactive");
		command.createArgument().setValue("-v");

		if (getTag() != null)
		{
			command.createArgument().setValue("-r");
			command.createArgument().setValue(getTag());
		}

		if (repo.getUser() != null)
		{
			command.createArgument().setValue("--username");
			command.createArgument().setValue(repo.getUser());
		}
		if (repo.getPassword() != null)
		{
			command.createArgument().setValue("--password");
			command.createArgument().setValue(repo.getPassword());
		}

		command.createArgument().setValue(repo.getUrl());

		return command;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getName()
     */
    public String getName()
    {
        return NAME;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getDisplayName()
     */
    public String getDisplayName()
    {
        return "Update";
    }

}
