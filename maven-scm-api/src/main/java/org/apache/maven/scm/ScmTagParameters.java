package org.apache.maven.scm;

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

import java.io.Serializable;

/**
 * @author Olivier Lamy
 * @since 1.2
 */
public class ScmTagParameters
    implements Serializable
{
    private static final long serialVersionUID = 7241536408630606807L;

    private String message;

    private boolean remoteTagging = false;

    private boolean pinExternals = false;

    private String scmRevision;

    public ScmTagParameters()
    {
        this.remoteTagging = false;
        this.pinExternals = false;
    }

    public ScmTagParameters( String message )
    {
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    public boolean isRemoteTagging()
    {
        return remoteTagging;
    }

    public void setRemoteTagging( boolean remoteTagging )
    {
        this.remoteTagging = remoteTagging;
    }

    public boolean isPinExternals()
    {
        return pinExternals;
    }

    public void setPinExternals( boolean pinExternals)
    {
        this.pinExternals = pinExternals;
    }

    public String getScmRevision()
    {
        return scmRevision;
    }

    public void setScmRevision( String scmRevision )
    {
        this.scmRevision = scmRevision;
    }

    public String toString()
    {
        return "[" + scmRevision + "] " + message;
    }
}
