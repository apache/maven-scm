package org.apache.maven.scm.command.changelog;

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

import org.apache.maven.scm.ScmResult;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author Olivier Lamy
 *
 */
public class ChangeLogScmResult
    extends ScmResult
{
    private static final long serialVersionUID = 559431861541372265L;
    private ChangeLogSet changeLog;

    public ChangeLogScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public ChangeLogScmResult( String commandLine, ChangeLogSet changeLog )
    {
        super( commandLine, null, null, true );

        this.changeLog = changeLog;
    }

    public ChangeLogScmResult( ChangeLogSet changeLog, ScmResult result )
    {
        super( result );

        this.changeLog = changeLog;
    }

    public ChangeLogSet getChangeLog()
    {
        return changeLog;
    }
}
