package org.apache.maven.scm.provider.clearcase.command;

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
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.clearcase.repository.ClearcaseRepository;
import org.apache.maven.scm.repository.Repository;
import org.apache.maven.scm.util.Commandline;
import org.apache.maven.scm.util.StreamConsumer;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractClearcaseCommand extends AbstractCommand
{
	private ClearcaseRepository repository;
	private String branchName;
	private String tagName;

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#setRepository(org.apache.maven.scm.repository.Repository)
     */
    public void setRepository(Repository repository) throws ScmException
    {
		if (repository instanceof ClearcaseRepository)
		{
			this.repository = (ClearcaseRepository)repository;
		}
		else
		{
			throw new ScmException("Invalid repository format");
		}
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getRepository()
     */
    public Repository getRepository()
    {
        return repository;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#setBranch(java.lang.String)
     */
    public void setBranch(String branchName)
    {
        this.branchName = branchName;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getBranch()
     */
    public String getBranch()
    {
        return branchName;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#setTag(java.lang.String)
     */
    public void setTag(String tagName)
    {
        this.tagName = tagName;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.Command#getTag()
     */
    public String getTag()
    {
        return tagName;
    }
}
