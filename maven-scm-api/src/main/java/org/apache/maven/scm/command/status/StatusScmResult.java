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
package org.apache.maven.scm.command.status;

import java.util.Collections;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmResult;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class StatusScmResult extends ScmResult {
    private static final long serialVersionUID = 7152442589455369403L;

    private List<ScmFile> changedFiles;

    public StatusScmResult(String commandLine, String providerMessage, String commandOutput, boolean success) {
        super(commandLine, providerMessage, commandOutput, success);

        this.changedFiles = Collections.emptyList();
    }

    public StatusScmResult(String commandLine, List<ScmFile> changedFiles) {
        super(commandLine, null, null, true);

        if (changedFiles == null) {
            throw new NullPointerException("changedFiles can't be null.");
        }

        this.changedFiles = changedFiles;
    }

    public StatusScmResult(List<ScmFile> changedFiles, ScmResult result) {
        super(result);

        if (changedFiles == null) {
            throw new NullPointerException("changedFiles can't be null.");
        }

        this.changedFiles = changedFiles;
    }

    public List<ScmFile> getChangedFiles() {
        return changedFiles;
    }
}
