package org.apache.maven.scm.provider.cvslib.command.changelog;

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
import org.apache.maven.scm.provider.cvslib.command.AbstractCvsCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsRepository;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsChangeLogCommand
    extends AbstractCvsCommand
    implements ChangeLogCommand
{
	private ChangeLogConsumer consumer;
    private Date startDate;
    private Date endDate;
    
    public CvsChangeLogCommand()
        throws ScmException
    {
        setConsumer(new CvsChangeLogConsumer());
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setRange(int numDays)
    {
        setStartDate(
            new Date(
                System.currentTimeMillis()
                    - (long)numDays * 24 * 60 * 60 * 1000));
        setEndDate(
            new Date(
                System.currentTimeMillis() + (long)1 * 24 * 60 * 60 * 1000));
    }

    public String getName()
    {
        return NAME;
    }

    public String getDisplayName()
    {
        return "ChangeLog";
    }

    public Commandline getCommandLine()
        throws ScmException
    {
        Commandline cl = new Commandline();
        cl.setExecutable("cvs");

        if (getWorkingDirectory() != null)
        {
            cl.setWorkingDirectory(getWorkingDirectory());
        }

        CvsRepository repo = (CvsRepository)getRepository();
        if (repo.getCvsRoot() != null)
        {
            cl.createArgument().setValue("-d");
            cl.createArgument().setValue(repo.getCvsRoot());
        }

        cl.createArgument().setValue("-q");

        cl.createArgument().setValue("log");

        if (startDate != null)
        {
            SimpleDateFormat outputDate = new SimpleDateFormat("yyyy-MM-dd");
            String dateRange;
            if (endDate == null)
            {
                dateRange = ">" + outputDate.format(getStartDate());
            }
            else
            {
                dateRange =
                    outputDate.format(getStartDate())
                        + "<"
                        + outputDate.format(getEndDate());
            }
            cl.createArgument().setValue("-d" + dateRange);
        }

        if (getTag() != null)
        {
            cl.createArgument().setValue("-r" + getTag());
        }

        return cl;
    }
    
    public void setConsumer(StreamConsumer consumer)
        throws ScmException
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

    public StreamConsumer getConsumer()
    {
        return consumer;
    }
}
