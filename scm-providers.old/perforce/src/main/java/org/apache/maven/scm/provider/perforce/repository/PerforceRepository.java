package org.apache.maven.scm.provider.perforce.repository;

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
public class PerforceRepository extends AbstractRepository
{
    private String password;
    private String path;
    private String p4port;
    private String user;
    
    /* (non-Javadoc)
     * Repository connection syntax:
     * scm:perforce[:host:port]:[username@]//depot/projects/name/...
     *
     * @see org.apache.maven.scm.repository.Repository#parseConnection()
     */
    public void parseConnection() throws ScmException
    {
        String conn = getConnection();
        
        if (conn.indexOf(getDelimiter()) > 1)
        {
            int lastDelimiter = conn.lastIndexOf(getDelimiter());
            path = conn.substring(lastDelimiter+1);
            p4port = conn.substring(0, lastDelimiter);
            if (p4port.indexOf(getDelimiter()) <= 0)
            {
                throw new ScmException("Connection string doesn't respect the Perforce scm format");
            }
        }
        else
        {
            path = conn;
            p4port = null;
        }
        if (path.indexOf("@") > 1)
        {
            user = path.substring(0, path.indexOf("@"));
            path = path.substring(path.indexOf("@") + 1);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.repository.Repository#getPassword()
     */
    public String getPassword()
    {
        return password;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.repository.Repository#setPassword(java.lang.String)
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public void setUser(String user)
    {
        this.user = user;
    }
    
    public String getUser()
    {
        return user;
    }
    
    public void setPath(String path)
    {
        this.path = path;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public String getHost()
    {
        if (p4port != null)
        {
            return p4port.substring(0, p4port.indexOf(getDelimiter()));
        }
        else
        {
            return null;
        }
    }
    
    public String getPort()
    {
        if (p4port != null)
        {
            return p4port.substring(p4port.indexOf(getDelimiter()) + 1);
        }
        else
        {
            return null;
        }
    }
}