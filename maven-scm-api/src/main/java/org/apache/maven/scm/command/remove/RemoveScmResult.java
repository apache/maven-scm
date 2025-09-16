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
package org.apache.maven.scm.command.remove;

import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmResult;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author Olivier Lamy
 */
public class RemoveScmResult extends ScmResult {
    private static final long serialVersionUID = 8852310735079996771L;

    private List<ScmFile> removedFiles;

    public RemoveScmResult(String commandLine, String providerMessage, String commandOutput, boolean success) {
        super(commandLine, providerMessage, commandOutput, success);
    }

    public RemoveScmResult(String commandLine, List<ScmFile> removedFiles) {
        super(commandLine, null, null, true);

        this.removedFiles = removedFiles;
    }

    public RemoveScmResult(List<ScmFile> removedFiles, ScmResult result) {
        super(result);

        this.removedFiles = removedFiles;
    }

    public List<ScmFile> getRemovedFiles() {
        return removedFiles;
    }
}
