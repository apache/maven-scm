package org.apache.maven.scm.provider.cvslib.command.checkout;

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
import org.apache.maven.scm.provider.cvslib.command.AbstractCvsCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsCheckOutCommand
    extends AbstractCvsCommand
    implements CheckOutCommand
{

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.AbstractCommand#getCommandLine()
     */
    public Commandline getCommandLine() throws ScmException
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

		cl.createArgument().setValue("checkout");

		if (getTag() != null)
		{
			cl.createArgument().setValue("-r" + getTag());
		}
		
		cl.createArgument().setValue(repo.getModule());

		return cl;
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
