package org.apache.maven.scm.provider.clearcase.command.changelog;

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
import org.apache.maven.scm.provider.clearcase.command.AbstractClearcaseCommand;
import org.apache.maven.scm.provider.clearcase.repository.ClearcaseRepository;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ClearcaseChangeLogCommand
    extends AbstractClearcaseCommand
    implements ChangeLogCommand
{
	private ChangeLogConsumer consumer;
    private Date startDate;
    private Date endDate;
    
    public ClearcaseChangeLogCommand() throws ScmException
    {
        super();
        endDate = new Date(
            System.currentTimeMillis() + (long)1 * 24 * 60 * 60 * 1000);
        
        setConsumer(new ClearcaseChangeLogConsumer());
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#setStartDate(java.util.Date)
     */
    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#getStartDate()
     */
    public Date getStartDate()
    {
        return startDate;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#setEndDate(java.util.Date)
     */
    public void setEndDate(Date endDate)
    {
        //Do nothing
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#getEndDate()
     */
    public Date getEndDate()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#setRange(int)
     */
    public void setRange(int numDays)
    {
        setStartDate(
            new Date(
                System.currentTimeMillis()
                    - (long)numDays * 24 * 60 * 60 * 1000));
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
        command.setExecutable("cleartool");
        command.createArgument().setValue("lshistory");
        
		if (getWorkingDirectory() != null)
		{
			command.setWorkingDirectory(getWorkingDirectory());
		}
        
        StringBuffer format = new StringBuffer();
        format.append("NAME:%En\\n");
        format.append("DATE:%Nd\\n");    
        format.append("COMM:%-12.12o - %o - %c - Activity: %[activity]p\\n");
        format.append("USER:%-8.8u\\n");

        command.createArgument().setValue("-fmt");
        command.createArgument().setValue(format.toString());
        command.createArgument().setValue("-recurse");
        command.createArgument().setValue("-nco");

        if (getStartDate() != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            String start = sdf.format(startDate);
            
            command.createArgument().setValue("-since");
            command.createArgument().setValue(start);
        }

        if (getBranch() != null)
        {
            command.createArgument().setValue("-branch");
            command.createArgument().setValue(getBranch());
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