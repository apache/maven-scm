package org.apache.maven.scm.repository;

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

import java.io.Serializable;

import org.apache.maven.scm.ScmException;

public class RepositoryInfo implements Serializable
{
    public static final String URL_STARTER = "scm";

    private String scmUrl;
    private String delimiter;
    private String scmType;
    private String connectionString;
    private String password;

    public RepositoryInfo()
    {
    }

    public RepositoryInfo(String scmUrl) throws ScmException
    {
        this.scmUrl = scmUrl;
        checkConnection(scmUrl);
    }

    public void setUrl(String scmUrl) throws ScmException
    {
        checkConnection(scmUrl);
        this.scmUrl = scmUrl;
    }

    public String getUrl()
    {
        return scmUrl;
    }

    public String getDelimiter()
    {
        return delimiter;
    }

    public String getType()
    {
        return scmType;
    }

    public String getConnection()
    {
        return connectionString;
    }
    
    public void setPassword(String password)
    {
    	this.password = password;
    }
    
    public String getPassword()
    {
    	return password;
    }

    private void checkConnection(String scmUrl) throws ScmException
    {
        String url = scmUrl;

        if (url == null)
        {
            throw new NullPointerException("repository connection is null");
        }
        if (!url.startsWith(URL_STARTER))
        {
            throw new IllegalArgumentException(
                "repository connection must start with "
                    + URL_STARTER
                    + "[delim]");
        }
        if (url.length() < URL_STARTER.length() + 1)
        {
            throw new IllegalArgumentException("repository connection is too short");
        }

        // Find token delimiter
        delimiter = url.substring(URL_STARTER.length(), URL_STARTER.length() + 1);
        url = url.substring(URL_STARTER.length() + 1);

        // Find SCM type (cvs, svn...)
        if (url.indexOf(delimiter) > 0)
        {
            scmType = url.substring(0, url.indexOf(delimiter));
            connectionString = url.substring(url.indexOf(delimiter) + 1);
        }
        else
        {
            throw new IllegalArgumentException(
                "repository connection must start with "
                    + URL_STARTER
                    + "[delim][scm type][delim]");
        }
    }
}