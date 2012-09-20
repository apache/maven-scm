package org.apache.maven.scm.command.diff;

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

import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmResult;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class DiffScmResult
    extends ScmResult
{
    private static final long serialVersionUID = 4036970486972633082L;

    private List<ScmFile> changedFiles;

    private Map<String,CharSequence> differences;

    private String patch;

    public DiffScmResult( String commandLine, List<ScmFile> changedFiles, Map<String,CharSequence> differences, String patch )
    {
        this( commandLine, null, null, true );
        this.changedFiles = changedFiles;
        this.differences = differences;
        this.patch = patch;
    }

    public DiffScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public DiffScmResult( List<ScmFile> changedFiles, Map<String,CharSequence> differences, String patch, ScmResult result )
    {
        super( result );

        this.changedFiles = changedFiles;

        this.differences = differences;

        this.patch = patch;
    }

    public List<ScmFile> getChangedFiles()
    {
        return changedFiles;
    }

    public Map<String,CharSequence> getDifferences()
    {
        return differences;
    }

    public String getPatch()
    {
        return patch;
    }
}
