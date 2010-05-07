package org.apache.maven.scm.provider.accurev.command.update;

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

import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevVersion;

/**
 * Carry information about before and after transaction ids so we can run the changelog
 * 
 * @author ggardner
 */
public class AccuRevUpdateScmResult
    extends UpdateScmResult
{

    private AccuRevVersion fromVersion;

    private AccuRevVersion toVersion;

    public AccuRevUpdateScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public AccuRevUpdateScmResult( AccuRevVersion startVersion, AccuRevVersion endVersion, String commandLines,
                                   List<ScmFile> updatedFiles )
    {
        super( commandLines, updatedFiles );
        this.fromVersion = startVersion;
        this.toVersion = endVersion;
    }

    public AccuRevVersion getFromVersion()
    {
        return fromVersion;
    }

    public AccuRevVersion getToVersion()
    {
        return toVersion;
    }
}