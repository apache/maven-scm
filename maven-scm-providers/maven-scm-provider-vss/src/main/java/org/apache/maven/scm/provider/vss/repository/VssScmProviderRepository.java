package org.apache.maven.scm.provider.vss.repository;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:triek@thrx.de">Thorsten Riek</a>
 *
 */
public class VssScmProviderRepository
    extends ScmProviderRepository
{
    private String vssdir;

    private String project;

    public VssScmProviderRepository( String user, String password, String vssdir, String project )
    {
        super();
        setUser( user );
        setPassword( password );
        this.vssdir = StringUtils.replace( vssdir, "/", "\\" );
//        this.project = StringUtils.replace( project, "/", "\\" );
        this.project = project;
    }

    public String getProject()
    {
        return project;
    }

    public String getVssdir()
    {
        return vssdir;
    }

    public String getUserPassword()
    {
        String userPassword = null;

        if ( !StringUtils.isEmpty( getUser() ) )
        {
            userPassword = getUser();

            if ( !StringUtils.isEmpty( getPassword() ) )
            {
                userPassword += "," + getPassword();
            }
        }
        return userPassword;
    }
}
