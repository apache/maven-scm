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
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface Command
{
    String ROLE = Command.class.getName();
    
    String getName() throws Exception;
    
    String getDisplayName() throws Exception;
    
    void setRepository(Repository repository) throws ScmException;
    
    Repository getRepository();
    
    void setWorkingDirectory(String workingDir);
    
    String getWorkingDirectory();
    
    void setBranch(String branchName);

    String getBranch();

    void setTag(String tagName);

    String getTag();
    
    //void setLogger(Logger logger);
    
    //Logger getLogger();
    
    void execute() throws Exception;

	void setConsumer(StreamConsumer consumer) throws ScmException;

	StreamConsumer getConsumer();
	
	Commandline getCommandLine() throws ScmException;
}
