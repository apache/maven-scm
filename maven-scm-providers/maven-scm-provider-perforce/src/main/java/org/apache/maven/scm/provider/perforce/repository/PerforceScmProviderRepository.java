package org.apache.maven.scm.provider.perforce.repository;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PerforceScmProviderRepository
    extends ScmProviderRepository
{
    private String host;

    private int port;

    private String path;

    private String user;

    private String password;

    public PerforceScmProviderRepository( String host, int port, String path, String user, String password )
    {
        this.host = host;

        this.port = port;

        this.path = path;

        this.user = user;

        this.password = password;
    }

    // ----------------------------------------------------------------------
    // ScmProviderRepository Implementation
    // ----------------------------------------------------------------------

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getPath()
    {
        return path;
    }

    public String getUser()
    {
        return user;
    }

    public String getPassword()
    {
        return password;
    }
}
