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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.Repository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class TestCommand extends AbstractCommand
{

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.AbstractCommand#getCommandLine()
     */
    public Commandline getCommandLine() throws ScmException
    {
        Commandline cmd = new Commandline();
        //cmd.setWorkingDirectory(getWorkingDirectory());
        cmd.setExecutable("echo");
        cmd.createArgument().setValue("Hello");
        return cmd;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getName()
     */
    public String getName() throws Exception
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getDisplayName()
     */
    public String getDisplayName() throws Exception
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#setRepository(org.apache.maven.scm.repository.Repository)
     */
    public void setRepository(Repository repository) throws ScmException
    {
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getRepository()
     */
    public Repository getRepository()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#setBranch(java.lang.String)
     */
    public void setBranch(String branchName)
    {
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getBranch()
     */
    public String getBranch()
    {
       return null;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#setTag(java.lang.String)
     */
    public void setTag(String tagName)
    {
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getTag()
     */
    public String getTag()
    {
        return null;
    }
}
