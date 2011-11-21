package org.apache.maven.scm.provider.git;

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

import org.apache.maven.scm.provider.git.command.GitCommand;

import java.io.File;

public class TestGitScmProvider
    extends AbstractGitScmProvider
{
    protected GitCommand getAddCommand()
    {
        return null;
    }

    protected GitCommand getBranchCommand()
    {
        return null;
    }

    protected GitCommand getChangeLogCommand()
    {
        return null;
    }

    protected GitCommand getCheckInCommand()
    {
        return null;
    }

    protected GitCommand getCheckOutCommand()
    {
        return null;
    }

    protected GitCommand getDiffCommand()
    {
        return null;
    }

    protected GitCommand getExportCommand()
    {
        return null;
    }

    protected GitCommand getRemoveCommand()
    {
        return null;
    }

    protected GitCommand getStatusCommand()
    {
        return null;
    }

    protected GitCommand getTagCommand()
    {
        return null;
    }

    protected GitCommand getUpdateCommand()
    {
        return null;
    }

    protected GitCommand getListCommand()
    {
        return null;
    }

    protected GitCommand getInfoCommand()
    {
        return null;
    }

    protected GitCommand getBlameCommand()
    {
        return null;
    }
    
    public GitCommand getRemoteInfoCommand() {
    	return null;
    }

    protected String getRepositoryURL( File path )
    {
        return null;
    }
}
