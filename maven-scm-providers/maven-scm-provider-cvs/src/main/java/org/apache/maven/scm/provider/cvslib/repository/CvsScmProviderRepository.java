package org.apache.maven.scm.provider.cvslib.repository;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsScmProviderRepository
	extends ScmProviderRepository
{
    /** */
    private String cvsroot;

    /** */
    private String transport;

    /** */
    private String user;

    /** */
    private String host;

    /** */
    private String path;

    /** */
    private String module;

    public CvsScmProviderRepository( String cvsroot, String transport, String user, String host, String path, String module )
    {
        this.cvsroot = cvsroot;

        this.transport = transport;

        this.user = user;

        this.host = host;

        this.path = path;

        this.module = module;
    }

    public String getCvsRoot() throws ScmException
    {
        return cvsroot;
    }

    /**
     * @return The subtype (like pserver).
     * @throws ScmException
     */
    public String getTransport() throws ScmException
    {
        return transport;
    }

    /**
     * @return The user.
     * @throws ScmException
     */
    public String getUser() throws ScmException
    {
        return user;
    }

    /**
     * @return The host.
     * @throws ScmException
     */
    public String getHost() throws ScmException
    {
        return host;
    }

    /**
     * @return The path.
     * @throws ScmException
     */
    public String getPath() throws ScmException
    {
        return path;
    }

    /**
     * @return The module name.
     * @throws ScmException
     */
    public String getModule() throws ScmException
    {
        return module;
    }
    /*
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
    */
}
