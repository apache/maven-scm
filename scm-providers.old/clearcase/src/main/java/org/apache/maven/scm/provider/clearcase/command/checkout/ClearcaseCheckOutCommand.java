package org.apache.maven.scm.provider.clearcase.command.checkout;

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
import org.apache.maven.scm.provider.clearcase.command.AbstractClearcaseCommand;
import org.apache.maven.scm.provider.clearcase.repository.ClearcaseRepository;
import org.apache.maven.scm.util.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ClearcaseCheckOutCommand
    extends AbstractClearcaseCommand
    implements CheckOutCommand
{

    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.AbstractCommand#getCommandLine()
     */
    public Commandline getCommandLine() throws ScmException
    {
        Commandline command = new Commandline();
        command.setExecutable("cleartool");
        command.createArgument().setValue("co");
        
        if (getBranch() != null)
        {
            command.createArgument().setValue("-branch");
            command.createArgument().setValue(getBranch());
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
