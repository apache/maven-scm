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

import org.apache.maven.scm.ScmException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractRepository implements Repository
{
    private String urlConnection;
    private String delimiter;

    /* (non-Javadoc)
     * @see org.apache.maven.scm.repository.Repository#setConnection(java.lang.String)
     */
    public void setConnection(String urlConnection) throws ScmException
    {
        this.urlConnection = urlConnection;
        
        if (getDelimiter() == null)
        {
            throw new ScmException("delimiter must be defined");
        }
        
        parseConnection();
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.repository.Repository#getConnection()
     */
    public String getConnection()
    {
        return urlConnection;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.repository.Repository#setDelimiter(java.lang.String)
     */
    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.scm.repository.Repository#getDelimiter()
     */
    public String getDelimiter()
    {
        return delimiter;
    }
}