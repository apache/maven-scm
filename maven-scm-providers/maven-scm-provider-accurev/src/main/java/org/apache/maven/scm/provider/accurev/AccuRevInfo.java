package org.apache.maven.scm.provider.accurev;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;

public class AccuRevInfo
{

    private File basedir;

    private String user;

    private String workSpace;

    private String basis;

    private String top;

    private String server;

    private int port;

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public String getServer()
    {
        return server;
    }

    public void setServer( String server )
    {
        this.server = server;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser( String user )
    {
        this.user = user;
    }

    public String getWorkSpace()
    {
        return workSpace;
    }

    public void setWorkSpace( String workSpace )
    {
        this.workSpace = workSpace;
    }

    public String getBasis()
    {
        return basis;
    }

    public void setBasis( String basis )
    {
        this.basis = basis;
    }

    public String getTop()
    {
        return top;
    }

    public void setTop( String top )
    {
        this.top = top;
    }

    public File getBasedir()
    {
        return basedir;
    }

    public AccuRevInfo( File basedir )
    {
        this.basedir = basedir;
    }

    public boolean isWorkSpace()
    {
        return getWorkSpace() != null;
    }

    public boolean isWorkSpaceTop()
    {
        return ( getTop() != null ) && getBasedir().equals( new File( getTop() ) );
    }

}
