package org.apache.maven.scm.provider.starteam.repository;

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
import org.apache.maven.scm.repository.AbstractRepository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamRepository extends AbstractRepository
{
    private String password;
    private String starteamUrl;
    private String user;

    /* (non-Javadoc)
     * @see org.apache.maven.scm.repository.Repository#setPassword(java.lang.String)
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.repository.Repository#getPassword()
     */
    public String getPassword()
    {
        return password;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.repository.Repository#parseConnection()
     */
    public void parseConnection() throws ScmException
    {
        starteamUrl = getConnection();
        
        if (getConnection().indexOf("@") >= 1)
        {
            String userAndPassword = getConnection().substring(0, getConnection().indexOf("@"));
            if (userAndPassword.indexOf(":") >=1)
            {
                user = userAndPassword.substring(0, userAndPassword.indexOf(":"));
                setPassword(userAndPassword.substring(userAndPassword.indexOf(":")+1));
            }
            else
            {
                user = userAndPassword;
            }
        }
        else
        {
            throw new ScmException("Starteam URL doesn't respect the format : username[:password]@hostname:port/projectName/[viewName/][folderHierarchy/]");
        }
    }

    public String getUser() throws ScmException
    {
        parseConnection();
        return user;
    }

    public String getUrl() throws ScmException
    {
        parseConnection();
        return starteamUrl;
    }
}
