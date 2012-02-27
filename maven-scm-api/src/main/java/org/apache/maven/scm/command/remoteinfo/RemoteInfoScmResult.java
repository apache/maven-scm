package org.apache.maven.scm.command.remoteinfo;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Olivier Lamy
 * @since 1.6
 */
public class RemoteInfoScmResult
    extends ScmResult
{

    /**
     * depending on scm informations can be different
     * svn: branch name / remote url
     */
    private Map<String, String> branches = new HashMap<String, String>();

    /**
     * depending on scm informations can be different
     * svn: branch name / remote url
     */
    private Map<String, String> tags = new HashMap<String, String>();

    public RemoteInfoScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public RemoteInfoScmResult( String commandLine, Map<String, String> branches, Map<String, String> tags )
    {
        super( commandLine, null, null, true );
        this.branches = branches;
        this.tags = tags;
    }

    public Map<String, String> getBranches()
    {
        return branches;
    }

    public void setBranches( Map<String, String> branches )
    {
        this.branches = branches;
    }

    public Map<String, String> getTags()
    {
        return tags;
    }

    public void setTags( Map<String, String> tags )
    {
        this.tags = tags;
    }
}
