package org.apache.maven.scm.provider.tfs;

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

public class TfsScmProviderRepository
    extends ScmProviderRepository
{
    private final String tfsUrl;

    private final String serverPath;

    private final String workspace;

    private final boolean useCheckinPolicies;

    public TfsScmProviderRepository( String tfsUrl, String user, String password, String serverPath, String workspace,
            boolean useCheckinPolicies )
    {
        super();
        setUser( user );
        setPassword( password );
        this.tfsUrl = tfsUrl;
        this.serverPath = serverPath;
        this.workspace = workspace;
        this.useCheckinPolicies = useCheckinPolicies;
    }

    public String getTfsUrl()
    {
        return tfsUrl;
    }

    public String getWorkspace()
    {
        return workspace;
    }

    public String getServerPath()
    {
        return serverPath;
    }

    public String getUserPassword()
    {
        String userPassword = null;

        if ( !StringUtils.isEmpty( getUser() ) )
        {
            userPassword = getUser();

            if ( !StringUtils.isEmpty( getPassword() ) )
            {
                userPassword += ";" + getPassword();
            }
        }
        return userPassword;
    }

    public boolean isUseCheckinPolicies() 
    {
        return useCheckinPolicies;
    }
}
