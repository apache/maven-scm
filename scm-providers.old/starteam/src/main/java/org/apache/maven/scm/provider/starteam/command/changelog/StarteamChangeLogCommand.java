package org.apache.maven.scm.provider.starteam.command.changelog;

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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogConsumer;
import org.apache.maven.scm.provider.starteam.command.AbstractStarteamCommand;
import org.apache.maven.scm.provider.starteam.repository.StarteamRepository;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamChangeLogCommand
    extends AbstractStarteamCommand
    implements ChangeLogCommand
{
	private ChangeLogConsumer consumer;

    public StarteamChangeLogCommand() throws ScmException
    {
        setConsumer(new StarteamChangeLogConsumer());
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#setStartDate(java.util.Date)
     */
    public void setStartDate(Date startDate)
    {
        if (consumer instanceof StarteamChangeLogConsumer)
        {
            ((StarteamChangeLogConsumer)consumer).setStartDate(startDate);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#getStartDate()
     */
    public Date getStartDate()
    {
        if (consumer instanceof StarteamChangeLogConsumer)
        {
            return ((StarteamChangeLogConsumer)consumer).getStartDate();
        }
        else
        {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#setEndDate(java.util.Date)
     */
    public void setEndDate(Date endDate)
    {
        if (consumer instanceof StarteamChangeLogConsumer)
        {
            ((StarteamChangeLogConsumer)consumer).setEndDate(endDate);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#getEndDate()
     */
    public Date getEndDate()
    {
        if (consumer instanceof StarteamChangeLogConsumer)
        {
            return ((StarteamChangeLogConsumer)consumer).getEndDate();
        }
        else
        {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#setRange(int)
     */
    public void setRange(int numDays)
    {
        if (consumer instanceof StarteamChangeLogConsumer)
        {
            ((StarteamChangeLogConsumer)consumer).setDateRange(numDays);
        }
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
    public String getDisplayName() throws Exception
    {
        return "ChangeLog";
    }

    public Commandline getCommandLine() throws ScmException
    {
        Commandline command = new Commandline();
        command.setExecutable("stcmd");
        
		if (getWorkingDirectory() != null)
		{
			command.setWorkingDirectory(getWorkingDirectory());
		}
		
        command.createArgument().setValue("hist");
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
     * @see org.apache.maven.scm.command.Command#setConsumer(org.codehaus.plexus.util.cli.StreamConsumer)
     */
    public void setConsumer(StreamConsumer consumer) throws ScmException
    {
		if (consumer instanceof ChangeLogConsumer)
		{
			this.consumer = (ChangeLogConsumer)consumer;
		}
		else
		{
			throw new ScmException("Unsupported consumer for this command");
		}
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getConsumer()
     */
    public StreamConsumer getConsumer()
    {
        return consumer;
    }
}