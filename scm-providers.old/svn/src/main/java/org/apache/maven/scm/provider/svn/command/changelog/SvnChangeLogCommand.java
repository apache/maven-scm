package org.apache.maven.scm.provider.svn.command.changelog;

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
import java.util.TimeZone;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogConsumer;
import org.apache.maven.scm.provider.svn.command.AbstractSvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnRepository;
import org.apache.maven.scm.util.Commandline;
import org.apache.maven.scm.util.StreamConsumer;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeLogCommand
    extends AbstractSvnCommand
    implements ChangeLogCommand
{
    public SvnChangeLogCommand() throws ScmException
    {
        setConsumer(new SvnChangeLogConsumer());
    }

    /** Date format expected by Subversion */
    static final SimpleDateFormat SVN_DATE_FORMAT_IN =
        new SimpleDateFormat("yyyy/MM/dd 'GMT'");

    /** Set the time zone of the formatters to GMT. */
    static {
        SVN_DATE_FORMAT_IN.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private ChangeLogConsumer consumer;
    private Date startDate;
    private Date endDate;

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
        this.endDate = endDate;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogCommand#getEndDate()
     */
    public Date getEndDate()
    {
        return endDate;
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
        setEndDate(
            new Date(
                System.currentTimeMillis() + (long)1 * 24 * 60 * 60 * 1000));
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
        return "Changelog";
    }

    public Commandline getCommandLine() throws ScmException
    {
        Commandline command = new Commandline();

        command.setExecutable("svn");

        if (getWorkingDirectory() != null)
        {
            command.setWorkingDirectory(getWorkingDirectory());
        }

        SvnRepository repo = (SvnRepository)getRepository();

        command.createArgument().setValue("log");
        command.createArgument().setValue("--non-interactive");
        command.createArgument().setValue("-v");

        if (startDate != null)
        {
            command.createArgument().setValue("-r");
            if (endDate != null)
            {
                command.createArgument().setValue(
                    "{"
                        + SVN_DATE_FORMAT_IN.format(getStartDate())
                        + "}"
                        + ":{"
                        + SVN_DATE_FORMAT_IN.format(getEndDate())
                        + "}");
            }
            else
            {
                command.createArgument().setValue(
                    "{"
                        + SVN_DATE_FORMAT_IN.format(getStartDate())
                        + "}"
                        + ":HEAD");
            }
        }
        else
        {
            if (getTag() != null)
            {
                command.createArgument().setValue("-r");
                command.createArgument().setValue(getTag());
            }
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
     * @see org.apache.maven.scm.command.Command#setConsumer(org.apache.maven.scm.util.StreamConsumer)
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
