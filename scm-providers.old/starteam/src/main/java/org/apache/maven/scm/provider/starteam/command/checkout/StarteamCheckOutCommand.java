package org.apache.maven.scm.provider.starteam.command.checkout;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.checkout.CheckOutCommand;
import org.apache.maven.scm.provider.starteam.command.AbstractStarteamCommand;
import org.apache.maven.scm.provider.starteam.repository.StarteamRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamCheckOutCommand
    extends AbstractStarteamCommand
    implements CheckOutCommand
{

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.AbstractCommand#getCommandLine()
     */
    public Commandline getCommandLine() throws ScmException
    {
        Commandline command = new Commandline();
        command.setExecutable("stcmd");
        
		if (getWorkingDirectory() != null)
		{
			command.setWorkingDirectory(getWorkingDirectory());
		}
		
        command.createArgument().setValue("co");
        command.createArgument().setValue("-x");
        command.createArgument().setValue("-nologo");
        command.createArgument().setValue("-is");
        command.createArgument().setValue("-p");
        command.createArgument().setValue(((StarteamRepository)getRepository()).getUrl());
        
        if (getTag() != null)
        {
            command.createArgument().setValue("-vl");
            command.createArgument().setValue(getTag());
        }
        return command;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getName()
     */
    public String getName() throws Exception
    {
        return NAME;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getDisplayName()
     */
    public String getDisplayName() throws Exception
    {
        return "Check out";
    }
}
