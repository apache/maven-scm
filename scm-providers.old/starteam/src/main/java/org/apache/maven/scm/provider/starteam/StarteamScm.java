package org.apache.maven.scm.provider.starteam;

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
import org.apache.maven.scm.Scm;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.CommandWrapper;
import org.apache.maven.scm.repository.Repository;
import org.apache.maven.scm.repository.RepositoryInfo;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandWrapper;
import org.apache.maven.scm.provider.starteam.repository.StarteamRepository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamScm implements Scm
{
    private String scmType = "starteam";
    
    /**
     * Returns "starteam".
     *
     * @return the name of supported scm
     */
    public String getSupportedScm()
    {
        return scmType;
    }
    
    /**
     * Creates and returns new instance of CommandWrapper
     * @param repoInfo
     * @return Returns new instance of CommandWrapper
     */
    public CommandWrapper createCommandWrapper(RepositoryInfo repoInfo) throws ScmException
    {
        Repository repo = createRepository(repoInfo);
        StarteamCommandWrapper cw = new StarteamCommandWrapper();
        cw.setRepository(repo);
        return cw;
    }
    
    /**
     * Creates and returns new instance of Repository
     * @param repoInfo
     * @return Returns new instance of Repository
     */
    public Repository createRepository(RepositoryInfo repoInfo) throws ScmException
    {
        if (!scmType.equals(repoInfo.getType()))
        {
            throw new ScmException("This scm type isn't supported by this factory");
        }
        StarteamRepository repo = new StarteamRepository();
        repo.setDelimiter(repoInfo.getDelimiter());
        repo.setConnection(repoInfo.getConnection());
        repo.setPassword(repoInfo.getPassword());
        return repo;
    }
    
    /**
     * Creates and returns new instance of Command
     * @param repoInfo
     * @param commandName The command name like <tt>"changelog"</tt>, <tt>"checkout"</tt>.
     * @return Returns new instance of Repository
     */
    public Command createCommand(RepositoryInfo repoInfo, String commandName) throws ScmException
    {
        CommandWrapper cw = createCommandWrapper(repoInfo);
        return cw.getCommand(commandName);
    }
}
