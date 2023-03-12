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
package org.apache.maven.scm.command.edit;

import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmResult;

/**
 * @since 1.5 {@link ScmFile} will have the {@link org.apache.maven.scm.ScmFileStatus}
 *        {@link org.apache.maven.scm.ScmFileStatus#EDITED}
 */
public class EditScmResult extends ScmResult {
    private static final long serialVersionUID = -6274938710679161288L;

    private List<ScmFile> editFiles;

    public EditScmResult(String commandLine, String providerMessage, String commandOutput, boolean success) {
        super(commandLine, providerMessage, commandOutput, success);
    }

    public EditScmResult(String commandLine, List<ScmFile> editFiles) {
        super(commandLine, null, null, true);

        this.editFiles = editFiles;
    }

    public EditScmResult(List<ScmFile> editFiles, ScmResult result) {
        super(result);

        this.editFiles = editFiles;
    }

    public List<ScmFile> getEditFiles() {
        return editFiles;
    }
}
