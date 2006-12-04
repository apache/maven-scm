package org.apache.maven.scm.provider.perforce.repository;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PerforceScmProviderRepository
    extends ScmProviderRepositoryWithHost
{
    private String path;

    public PerforceScmProviderRepository( String host, int port, String path, String user, String password )
    {
        setHost( host );

        setPort( port );

        this.path = path;

        setUser( user );

        setPassword( password );
    }

    // ----------------------------------------------------------------------
    // ScmProviderRepository Implementation
    // ----------------------------------------------------------------------

    public String getPath()
    {
        return path;
    }
}
