package org.apache.maven.scm.provider.cvslib.command.tag;

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
import org.apache.maven.scm.command.tag.TagCommand;
import org.apache.maven.scm.provider.cvslib.command.AbstractCvsCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsTagCommand
    extends AbstractCvsCommand
    implements TagCommand
{
    private String tagName;

    public void setTagName(String name)
    {
        this.tagName = name;
    }
    
    public String getTagName()
    {
        return tagName;
    }
    
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

		cl.createArgument().setValue("tag");

		cl.createArgument().setValue("-c");

		if (getTagName() != null)
		{
			cl.createArgument().setValue(getTagName());
		}
		else
		{
		    throw new ScmException("You must specify a tag name");
		}
		
		return cl;
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
        return "Tag";
    }

}
