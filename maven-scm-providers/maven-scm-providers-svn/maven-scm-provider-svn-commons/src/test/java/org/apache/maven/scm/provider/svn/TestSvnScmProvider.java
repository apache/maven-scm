package org.apache.maven.scm.provider.svn;

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

import org.apache.maven.scm.provider.svn.command.SvnCommand;

import java.io.File;

public class TestSvnScmProvider
    extends AbstractSvnScmProvider
{
    protected SvnCommand getAddCommand()
    {
        return null;
    }

    protected SvnCommand getChangeLogCommand()
    {
        return null;
    }

    protected SvnCommand getCheckInCommand()
    {
        return null;
    }

    protected SvnCommand getCheckOutCommand()
    {
        return null;
    }

    protected SvnCommand getDiffCommand()
    {
        return null;
    }

    protected SvnCommand getRemoveCommand()
    {
        return null;
    }

    protected SvnCommand getStatusCommand()
    {
        return null;
    }

    protected SvnCommand getTagCommand()
    {
        return null;
    }

    protected SvnCommand getUpdateCommand()
    {
        return null;
    }

    protected SvnCommand getListCommand()
    {
        return null;
    }

    protected SvnCommand getInfoCommand()
    {
        return null;
    }

    protected String getRepositoryURL( File path )
    {
        return null;
    }
}
