package org.apache.maven.scm.command;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractCommandWrapper implements CommandWrapper
{
	private Hashtable commands = new Hashtable();
	
	/* (non-Javadoc)
	 * @see org.apache.maven.scm.command.CommandWrapper#getCommand(java.lang.String)
	 */
	public Command getCommand(String commandName) throws UnsupportedCommandException
	{
	    Command cmd = (Command)commands.get(commandName);
	    if (cmd != null)
	    {
	        return cmd;
	    }
	    else
	    {
	        throw new UnsupportedCommandException("commandName is an invalid command");
	    }
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.scm.command.CommandWrapper#getCommandNames()
	 */
	public Collection getCommandNames()
	{
		ArrayList names = new ArrayList();
		for(Enumeration enum=commands.keys(); enum.hasMoreElements(); )
		{
			names.add(enum.nextElement());
		}
		return names;
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.scm.command.CommandWrapper#getCommands()
	 */
	public Collection getCommands()
	{
		return commands.values();
	}

	public Hashtable getCommandsTable()
	{
		return commands;
	}
}
