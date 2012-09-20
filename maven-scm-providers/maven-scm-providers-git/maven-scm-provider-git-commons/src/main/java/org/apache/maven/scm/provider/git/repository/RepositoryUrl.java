package org.apache.maven.scm.provider.git.repository;

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

/**
 * This class is a container which holds information about 
 * repository URL. 
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 *
 * @since 1.3
 */
public class RepositoryUrl {


    /** the protocol used to access the upstream repository */
    private String protocol;
    
    /** the server to access the upstream repository */
    private String host;
    
    /** the port to access the upstream repository */
    private String port;
    
    /** the path on the server to access the upstream repository */
    private String path;
    
    /** the user name from the repository URL */
    private String userName;
    
    /** the password from the repository URL */
    private String password;
    
    public String getProtocol() 
    {
        return protocol;
    }

    public void setProtocol( String protocol ) 
    {
        this.protocol = protocol;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost( String host )
    {
        this.host = host;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort( String port )
    {
        this.port = port;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    
    
}
